package univ.fooddeliv.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import univ.fooddeliv.models.Dish;
import univ.fooddeliv.models.Order;
import univ.fooddeliv.models.OrderStatus;
import univ.fooddeliv.models.Restaurant;
import univ.fooddeliv.models.Role;
import univ.fooddeliv.models.User;
import univ.fooddeliv.repository.DishRepository;
import univ.fooddeliv.repository.OrderItemRepository;
import univ.fooddeliv.repository.OrderRepository;
import univ.fooddeliv.repository.RestaurantRepository;
import univ.fooddeliv.repository.RoleRepository;
import univ.fooddeliv.repository.UserRepository;
import univ.fooddeliv.utils.FileUtil;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final OrderRepository orderRepository;
    private final RestaurantRepository restaurantRepository;
    private final DishRepository dishRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final FileUtil fileUtil;
    private final PaymentService paymentService;

    @Transactional(readOnly = true)
    public List<Order> orders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Заказ не найден"));
        order.setStatus(status);
        order.setLiveTracking(false);
        if (status == OrderStatus.DELIVERED) {
            paymentService.captureOnDelivery(order);
            paymentService.assignReceipt(order);
        }
        if (status == OrderStatus.CANCELLED) {
            paymentService.refundOnline(order);
        }
        orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public List<Restaurant> restaurants() {
        return restaurantRepository.findAll(Sort.by("name"));
    }

    @Transactional(readOnly = true)
    public Restaurant restaurant(Long id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ресторан не найден"));
    }

    @Transactional
    public Restaurant saveRestaurant(Long id, String name, String address, String image, MultipartFile file) {
        String cleanName = required(name, "Укажите название ресторана");
        String cleanAddress = required(address, "Укажите адрес ресторана");
        Restaurant restaurant = id == null ? new Restaurant() : restaurant(id);
        restaurant.setName(cleanName);
        restaurant.setAddress(cleanAddress);
        restaurant.setImage(resolveImage(restaurant.getImage(), image, file));
        return restaurantRepository.save(restaurant);
    }

    @Transactional
    public void deleteRestaurant(Long id) {
        if (dishRepository.countByRestaurantId(id) > 0) {
            throw new IllegalArgumentException("Сначала удалите блюда этого ресторана");
        }
        restaurantRepository.delete(restaurant(id));
    }

    @Transactional(readOnly = true)
    public List<Dish> dishes() {
        return dishRepository.findAllByOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public Dish dish(Long id) {
        return dishRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Блюдо не найдено"));
    }

    @Transactional
    public Dish saveDish(Long id, Long restaurantId, String name, String description, Double price, String image, MultipartFile file) {
        if (restaurantId == null) {
            throw new IllegalArgumentException("Выберите ресторан");
        }
        if (price == null || price <= 0) {
            throw new IllegalArgumentException("Укажите цену больше нуля");
        }
        Dish dish = id == null ? new Dish() : dish(id);
        dish.setRestaurant(restaurant(restaurantId));
        dish.setName(required(name, "Укажите название блюда"));
        dish.setDescription(blankToNull(description));
        dish.setPrice(price);
        dish.setImage(resolveImage(dish.getImage(), image, file));
        return dishRepository.save(dish);
    }

    @Transactional
    public void deleteDish(Long id) {
        if (orderItemRepository.existsByDish_Id(id)) {
            throw new IllegalArgumentException("Блюдо уже есть в заказах, его нельзя удалить");
        }
        dishRepository.delete(dish(id));
    }

    @Transactional(readOnly = true)
    public List<User> users() {
        return userRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<Role> roles() {
        return roleRepository.findAll(Sort.by("id"));
    }

    @Transactional
    public void updateUserRole(Long userId, String roleName, String actorEmail) {
        User user = user(userId);
        Role role = roleRepository.findByRole(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Роль не найдена"));
        if (isSelf(user, actorEmail) && !"ADMIN".equals(role.getRole())) {
            throw new IllegalArgumentException("Нельзя снять с себя роль администратора");
        }
        if (isLastEnabledAdmin(user) && !"ADMIN".equals(role.getRole())) {
            throw new IllegalArgumentException("Должен остаться хотя бы один администратор");
        }
        user.setRole(role);
        userRepository.save(user);
    }

    @Transactional
    public void updateUserEnabled(Long userId, boolean enabled, String actorEmail) {
        User user = user(userId);
        if (isSelf(user, actorEmail) && !enabled) {
            throw new IllegalArgumentException("Нельзя отключить собственный аккаунт");
        }
        if (!enabled && isLastEnabledAdmin(user)) {
            throw new IllegalArgumentException("Нельзя отключить последнего администратора");
        }
        user.setEnabled(enabled);
        userRepository.saveAndFlush(user);
    }

    @Transactional
    public void deleteUser(Long userId, String actorEmail) {
        User user = user(userId);
        if (isSelf(user, actorEmail)) {
            throw new IllegalArgumentException("Нельзя удалить собственный аккаунт");
        }
        if (isLastEnabledAdmin(user)) {
            throw new IllegalArgumentException("Нельзя удалить последнего администратора");
        }
        String avatar = user.getAvatar();
        orderItemRepository.deleteByUserId(userId);
        orderRepository.deleteByUserId(userId);
        userRepository.deleteById(userId);
        try {
            fileUtil.deleteAvatar(avatar);
        } catch (IOException ignored) {
            // файл аватара не мешает удалению аккаунта
        }
    }

    private User user(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));
    }

    private boolean isSelf(User user, String actorEmail) {
        return user.getEmail() != null && user.getEmail().equalsIgnoreCase(actorEmail);
    }

    private boolean isLastEnabledAdmin(User user) {
        return user.getRole() != null
                && "ADMIN".equals(user.getRole().getRole())
                && Boolean.TRUE.equals(user.getEnabled())
                && userRepository.countEnabledByRole("ADMIN") <= 1;
    }

    private static String required(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private String resolveImage(String current, String typedPath, MultipartFile file) {
        if (file != null && !file.isEmpty()) {
            try {
                return fileUtil.saveCatalogImage(file);
            } catch (IOException e) {
                throw new IllegalArgumentException("Не удалось сохранить изображение");
            }
        }
        String typed = blankToNull(typedPath);
        return typed != null ? typed : current;
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
