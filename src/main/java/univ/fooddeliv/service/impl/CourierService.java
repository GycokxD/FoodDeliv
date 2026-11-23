package univ.fooddeliv.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import univ.fooddeliv.models.Order;
import univ.fooddeliv.models.OrderStatus;
import univ.fooddeliv.models.User;
import univ.fooddeliv.repository.OrderRepository;
import univ.fooddeliv.repository.UserRepository;
import univ.fooddeliv.utils.GeoUtil;
import univ.fooddeliv.utils.OrderUtil;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourierService {
    public static final String COURIER_ROLE = "COURIER";
    private static final List<OrderStatus> OPEN = List.of(OrderStatus.NEW, OrderStatus.COOKING);
    private static final List<OrderStatus> ACTIVE = List.of(OrderStatus.NEW, OrderStatus.COOKING, OrderStatus.ON_THE_WAY);
    private static final int MAX_ACTIVE_ORDERS = 3;
    private static final double DELIVERY_RADIUS_METERS = 500;
    private static final Duration MIN_LOCATION_INTERVAL = Duration.ofMillis(900);

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final PaymentService paymentService;
    private final OrderTrackingService orderTrackingService;

    @Transactional(readOnly = true)
    public List<Order> availableOrders() {
        return orderRepository.findByCourierIsNullAndLiveTrackingTrueAndLatitudeIsNotNullAndStatusInOrderByCreatedAtAsc(OPEN);
    }

    @Transactional(readOnly = true)
    public List<Order> activeOrders(String email) {
        return orderRepository.findByCourier_IdAndStatusInOrderByCreatedAtAsc(courier(email).getId(), ACTIVE);
    }

    @Transactional(readOnly = true)
    public Order orderForCourier(Long orderId, String email) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Заказ не найден"));
        if (!isAssignedTo(order, courier(email))) {
            throw new EntityNotFoundException("Заказ не найден");
        }
        return order;
    }

    @Transactional
    public Order take(Long orderId, String email) {
        User courier = courier(email);
        if (orderRepository.countByCourier_IdAndStatusIn(courier.getId(), ACTIVE) >= MAX_ACTIVE_ORDERS) {
            throw new IllegalArgumentException("Одновременно можно везти не больше " + MAX_ACTIVE_ORDERS + " заказов");
        }
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Заказ не найден"));
        if (order.getCourier() != null) {
            throw new IllegalArgumentException("Этот заказ уже взял другой курьер");
        }
        if (!OPEN.contains(order.getStatus()) || !Boolean.TRUE.equals(order.getLiveTracking())) {
            throw new IllegalArgumentException("Заказ уже нельзя взять");
        }
        order.setCourier(courier);
        Order saved = orderRepository.save(order);
        orderTrackingService.publish(saved.getId());
        return saved;
    }

    @Transactional
    public Order pickUp(Long orderId, String email) {
        Order order = assigned(orderId, email);
        if (order.getStatus() == OrderStatus.NEW) {
            throw new IllegalArgumentException("Ресторан ещё не принял заказ, подождите немного");
        }
        if (order.getStatus() != OrderStatus.COOKING) {
            throw new IllegalArgumentException("Заказ уже в другом статусе: " + order.getStatus().getLabel());
        }
        order.setStatus(OrderStatus.ON_THE_WAY);
        Order saved = orderRepository.save(order);
        orderTrackingService.publish(saved.getId());
        return saved;
    }

    @Transactional
    public Order deliver(Long orderId, String email) {
        Order order = assigned(orderId, email);
        if (order.getStatus() != OrderStatus.ON_THE_WAY) {
            throw new IllegalArgumentException("Сначала отметьте, что забрали заказ из ресторана");
        }
        OrderUtil.courierPoint(order).ifPresent(courierPoint -> OrderUtil.destinationPoint(order).ifPresent(destination -> {
            double meters = GeoUtil.distanceMeters(courierPoint[0], courierPoint[1], destination[0], destination[1]);
            if (meters > DELIVERY_RADIUS_METERS) {
                throw new IllegalArgumentException(String.format(
                        "Вы в %.1f км от адреса клиента. Отметить доставку можно рядом с ним", meters / 1000));
            }
        }));
        order.setStatus(OrderStatus.DELIVERED);
        paymentService.captureOnDelivery(order);
        paymentService.assignReceipt(order);
        Order saved = orderRepository.save(order);
        orderTrackingService.publish(saved.getId());
        return saved;
    }

    /**
     * @return false, если точка пришла слишком часто и была пропущена
     */
    @Transactional
    public boolean updateLocation(Long orderId, String email, double lat, double lng) {
        if (!GeoUtil.isValid(lat, lng)) {
            throw new IllegalArgumentException("Некорректные координаты");
        }
        Order order = assigned(orderId, email);
        if (!ACTIVE.contains(order.getStatus())) {
            throw new IllegalStateException("Заказ уже завершён");
        }
        LocalDateTime now = LocalDateTime.now();
        if (order.getCourierLocatedAt() != null
                && Duration.between(order.getCourierLocatedAt(), now).compareTo(MIN_LOCATION_INTERVAL) < 0) {
            return false;
        }
        order.setCourierLatitude(lat);
        order.setCourierLongitude(lng);
        order.setCourierLocatedAt(now);
        orderRepository.save(order);
        orderTrackingService.publish(order.getId());
        return true;
    }

    private Order assigned(Long orderId, String email) {
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Заказ не найден"));
        if (!isAssignedTo(order, courier(email))) {
            throw new EntityNotFoundException("Заказ не найден");
        }
        return order;
    }

    private User courier(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));
        if (user.getRole() == null || !COURIER_ROLE.equals(user.getRole().getRole())) {
            throw new IllegalArgumentException("Доступно только курьерам");
        }
        return user;
    }

    private static boolean isAssignedTo(Order order, User courier) {
        return order.getCourier() != null && order.getCourier().getId().equals(courier.getId());
    }
}
