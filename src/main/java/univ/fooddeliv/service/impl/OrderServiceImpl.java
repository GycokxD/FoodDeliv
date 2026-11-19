package univ.fooddeliv.service.impl;

import jakarta.persistence.EntityNotFoundException;
import univ.fooddeliv.dto.CheckoutDto;
import univ.fooddeliv.dto.DishDto;
import univ.fooddeliv.models.Order;
import univ.fooddeliv.models.OrderItem;
import univ.fooddeliv.models.OrderStatus;
import univ.fooddeliv.models.User;
import univ.fooddeliv.repository.OrderRepository;
import univ.fooddeliv.service.OrderItemService;
import univ.fooddeliv.service.OrderService;
import univ.fooddeliv.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {
    private final UserService userService;
    private final OrderRepository orderRepository;
    private final OrderItemService orderItemService;
    private final PaymentService paymentService;


    @Override
    public Order makeOrder(List<DishDto> dishesFromCookie, Authentication auth, CheckoutDto checkout) {
        if (dishesFromCookie == null || dishesFromCookie.isEmpty()) {
            throw new IllegalArgumentException("Корзина пуста");
        }
        long restaurants = dishesFromCookie.stream()
                .map(DishDto::getRestaurantId)
                .distinct()
                .count();
        if (restaurants > 1) {
            throw new IllegalArgumentException("В одном заказе могут быть блюда только из одного ресторана");
        }
        User user = userService.findByEmail(auth.getName());
        Order order = new Order();
        order.setUser(user);
        order.setTotalPrice(getTotalPrice(dishesFromCookie));
        order.setAddress(checkout.getAddress());
        order.setPhone(checkout.getPhone());
        order.setNote(checkout.getNote());
        order.setStatus(OrderStatus.NEW);
        order.setLiveTracking(true);
        paymentService.applyCheckout(order, checkout);
        Order createdOrder = orderRepository.save(order);
        paymentService.assignReceipt(createdOrder);
        for (DishDto dish : dishesFromCookie) {
            orderItemService.createOrderItem(dish, createdOrder);
        }
        if (user.getPhone() == null || user.getPhone().isBlank()) {
            user.setPhone(checkout.getPhone());
        }
        return createdOrder;
    }


    @Override
    public Double getTotalPrice(List<DishDto> dish) {
        if (dish != null && !dish.isEmpty()) {
            Double totalPrice = 0.0;
            for (DishDto d : dish) {
                totalPrice += d.getTotalPrice();
            }
            return totalPrice;
        }
        return 0.00;
    }

    @Override
    public Order getOrderByIdForUser(Long orderId, String email) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Заказ не найден"));
        boolean owner = order.getUser() != null && email.equals(order.getUser().getEmail());
        if (!owner) {
            var viewer = userService.findByEmail(email);
            boolean admin = viewer.getRole() != null && "ADMIN".equals(viewer.getRole().getRole());
            if (!admin) {
                throw new EntityNotFoundException("Заказ не найден");
            }
        }
        if (order.getStatus() == null) {
            order.setStatus(OrderStatus.NEW);
        }
        return order;
    }

    @Override
    public boolean cancelIfNew(Long orderId, String email) {
        Order order = getOrderByIdForUser(orderId, email);
        if (order.getStatus() != OrderStatus.NEW) {
            return false;
        }
        order.setStatus(OrderStatus.CANCELLED);
        order.setLiveTracking(false);
        paymentService.refundOnline(order);
        return true;
    }

    @Override
    public String toCartCookie(Long orderId, String email) {
        Order order = getOrderByIdForUser(orderId, email);
        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new IllegalArgumentException("В заказе нет блюд");
        }
        StringBuilder cart = new StringBuilder();
        for (OrderItem item : order.getItems()) {
            if (item.getDish() == null) {
                continue;
            }
            int quantity = item.getQuantity() == null || item.getQuantity() < 1 ? 1 : item.getQuantity();
            cart.append(item.getDish().getId()).append('#').append(quantity).append('!');
        }
        if (cart.isEmpty()) {
            throw new IllegalArgumentException("В заказе нет блюд");
        }
        return cart.toString();
    }
}
