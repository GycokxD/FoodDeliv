package univ.fooddeliv.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import univ.fooddeliv.config.MapsProperties;
import univ.fooddeliv.dto.OrderTrackingDto;
import univ.fooddeliv.dto.RouteDto;
import univ.fooddeliv.dto.TrackingStepDto;
import univ.fooddeliv.models.Order;
import univ.fooddeliv.models.OrderStatus;
import univ.fooddeliv.models.Restaurant;
import univ.fooddeliv.models.User;
import univ.fooddeliv.repository.OrderRepository;
import univ.fooddeliv.repository.UserRepository;
import univ.fooddeliv.utils.GeoUtil;
import univ.fooddeliv.utils.OrderUtil;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderTrackingService {
    private static final String[] DEMO_COURIERS = {"Азамат", "Нурлан", "Элина", "Бекзат"};

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final AppSettingsService appSettingsService;
    private final PaymentService paymentService;
    private final RoutingService routingService;
    private final MapsProperties mapsProperties;
    private final TrackingHub trackingHub;

    @Scheduled(fixedDelay = 3000)
    @Transactional
    public void advanceLiveOrders() {
        List<Order> live = orderRepository.findByLiveTrackingTrueAndStatusIn(
                List.of(OrderStatus.NEW, OrderStatus.COOKING, OrderStatus.ON_THE_WAY));
        for (Order order : live) {
            OrderStatus before = order.getStatus();
            advance(order);
            if (before != order.getStatus() || trackingHub.hasSubscribers(order.getId())) {
                trackingHub.publish(order.getId(), toDto(order));
            }
        }
    }

    @Transactional
    public void refresh(List<Order> orders) {
        if (orders == null) {
            return;
        }
        orders.forEach(this::advance);
    }

    @Transactional
    public OrderTrackingDto trackForUser(Long orderId, String email) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Заказ не найден"));
        if (order.getUser() == null || !canView(order, email)) {
            throw new EntityNotFoundException("Заказ не найден");
        }
        advance(order);
        return toDto(order);
    }

    @Transactional(readOnly = true)
    public void publish(Long orderId) {
        orderRepository.findById(orderId).ifPresent(order -> trackingHub.publish(orderId, toDto(order)));
    }

    private void advance(Order order) {
        if (!Boolean.TRUE.equals(order.getLiveTracking()) || order.getCreatedAt() == null) {
            return;
        }
        OrderStatus current = order.getStatus() == null ? OrderStatus.NEW : order.getStatus();
        if (current == OrderStatus.CANCELLED || current == OrderStatus.DELIVERED) {
            return;
        }
        OrderStatus next = statusFor(elapsedSeconds(order));
        if (waitsForRealCourier(order) && next.ordinal() > OrderStatus.COOKING.ordinal()) {
            next = OrderStatus.COOKING;
        }
        if (next.ordinal() > current.ordinal()) {
            order.setStatus(next);
            if (next == OrderStatus.DELIVERED) {
                paymentService.captureOnDelivery(order);
                paymentService.assignReceipt(order);
            }
            orderRepository.save(order);
        }
    }

    private OrderTrackingDto toDto(Order order) {
        OrderStatus status = order.getStatus() == null ? OrderStatus.NEW : order.getStatus();
        long elapsed = elapsedSeconds(order);
        boolean cancelled = status == OrderStatus.CANCELLED;
        boolean delivered = status == OrderStatus.DELIVERED;
        boolean waitingForCourier = waitsForRealCourier(order) && order.getCourier() == null
                && (status == OrderStatus.NEW || status == OrderStatus.COOKING);

        Optional<Restaurant> restaurant = OrderUtil.restaurantOf(order);
        Optional<double[]> restaurantPoint = OrderUtil.restaurantPoint(order);
        Optional<double[]> destinationPoint = OrderUtil.destinationPoint(order);

        List<double[]> route = List.of();
        Double routeDurationSeconds = null;
        if (restaurantPoint.isPresent() && destinationPoint.isPresent()) {
            RouteDto routed = routingService.route(
                    restaurantPoint.get()[0], restaurantPoint.get()[1],
                    destinationPoint.get()[0], destinationPoint.get()[1]);
            route = routed.points();
            routeDurationSeconds = routed.durationSeconds();
        }

        double[] courierPoint = resolveCourierPoint(order, status, elapsed, restaurantPoint, destinationPoint, route);
        String eta = eta(order, status, elapsed, courierPoint, destinationPoint, routeDurationSeconds);

        return OrderTrackingDto.builder()
                .status(status.name())
                .statusLabel(status.getLabel())
                .badge(status.getBadge())
                .message(message(status, waitingForCourier, order.getCourier() != null))
                .eta(eta)
                .progress(progress(status, elapsed, waitingForCourier))
                .live(Boolean.TRUE.equals(order.getLiveTracking()) && !delivered && !cancelled)
                .finished(delivered || cancelled)
                .canCancel(status == OrderStatus.NEW)
                .paid(Boolean.TRUE.equals(order.getPaid()))
                .paymentState(paymentState(order))
                .paymentBadge(paymentBadge(order))
                .courierName(courierName(order))
                .restaurantName(restaurant.map(Restaurant::getName).orElse("Ресторан"))
                .restaurantLat(restaurantPoint.map(p -> p[0]).orElse(null))
                .restaurantLng(restaurantPoint.map(p -> p[1]).orElse(null))
                .destinationLat(destinationPoint.map(p -> p[0]).orElse(null))
                .destinationLng(destinationPoint.map(p -> p[1]).orElse(null))
                .courierLat(courierPoint == null ? null : courierPoint[0])
                .courierLng(courierPoint == null ? null : courierPoint[1])
                .route(route)
                .centerLat(mapsProperties.centerLat())
                .centerLng(mapsProperties.centerLng())
                .showMap(!cancelled && (restaurantPoint.isPresent() || destinationPoint.isPresent()))
                .waitingForCourier(waitingForCourier)
                .steps(steps(status))
                .build();
    }

    private double[] resolveCourierPoint(Order order,
                                         OrderStatus status,
                                         long elapsed,
                                         Optional<double[]> restaurantPoint,
                                         Optional<double[]> destinationPoint,
                                         List<double[]> route) {
        if (status == OrderStatus.CANCELLED) {
            return null;
        }
        if (order.getCourier() != null) {
            return OrderUtil.courierPoint(order)
                    .or(() -> restaurantPoint)
                    .or(() -> destinationPoint)
                    .orElse(null);
        }
        if (!appSettingsService.demoCouriers()) {
            return restaurantPoint.orElse(null);
        }
        if (status == OrderStatus.DELIVERED) {
            return destinationPoint.or(() -> restaurantPoint).orElse(null);
        }
        if (status != OrderStatus.ON_THE_WAY) {
            return restaurantPoint.orElse(null);
        }
        double travel = travelProgress(status, elapsed);
        if (!route.isEmpty()) {
            return GeoUtil.pointAlong(route, travel);
        }
        if (restaurantPoint.isPresent() && destinationPoint.isPresent()) {
            double[] from = restaurantPoint.get();
            double[] to = destinationPoint.get();
            return new double[]{
                    from[0] + (to[0] - from[0]) * travel,
                    from[1] + (to[1] - from[1]) * travel
            };
        }
        return restaurantPoint.or(() -> destinationPoint).orElse(null);
    }

    private String eta(Order order,
                       OrderStatus status,
                       long elapsed,
                       double[] courierPoint,
                       Optional<double[]> destinationPoint,
                       Double routeDurationSeconds) {
        if (status == OrderStatus.DELIVERED) {
            return "Заказ у вас";
        }
        if (status == OrderStatus.CANCELLED) {
            return "Доставка остановлена";
        }
        if (waitsForRealCourier(order) && order.getCourier() == null) {
            return "ждём курьера";
        }
        if (order.getCourier() != null && status == OrderStatus.ON_THE_WAY
                && courierPoint != null && destinationPoint.isPresent()) {
            RouteDto remaining = routingService.route(
                    courierPoint[0], courierPoint[1],
                    destinationPoint.get()[0], destinationPoint.get()[1]);
            return formatMinutes(remaining.durationSeconds());
        }
        if (!waitsForRealCourier(order) && status == OrderStatus.ON_THE_WAY && routeDurationSeconds != null) {
            double left = routeDurationSeconds * (1 - travelProgress(status, elapsed));
            return formatMinutes(left);
        }
        long left = Math.max(0, deliveredAtSeconds() - elapsed);
        return formatMinutes(left);
    }

    private static String formatMinutes(double seconds) {
        if (seconds < 60) {
            return "меньше минуты";
        }
        long minutes = Math.round(seconds / 60.0);
        return "около " + minutes + " мин";
    }

    private String courierName(Order order) {
        User courier = order.getCourier();
        if (courier != null && courier.getName() != null && !courier.getName().isBlank()) {
            return courier.getName();
        }
        int index = (int) Math.floorMod(order.getId() == null ? 0 : order.getId(), DEMO_COURIERS.length);
        return DEMO_COURIERS[index];
    }

    private boolean waitsForRealCourier(Order order) {
        return order.getCourier() != null || !appSettingsService.demoCouriers();
    }

    private boolean canView(Order order, String email) {
        if (order.getUser() != null && email.equals(order.getUser().getEmail())) {
            return true;
        }
        return userRepository.findByEmail(email)
                .map(user -> user.getRole() != null && "ADMIN".equals(user.getRole().getRole()))
                .orElse(false);
    }

    private int acceptedSeconds() {
        return appSettingsService.acceptedSeconds();
    }

    private int cookingUntilSeconds() {
        return acceptedSeconds() + appSettingsService.cookingSeconds();
    }

    private int deliveredAtSeconds() {
        return cookingUntilSeconds() + appSettingsService.deliverySeconds();
    }

    private OrderStatus statusFor(long elapsed) {
        int accepted = appSettingsService.acceptedSeconds();
        int cookingUntil = accepted + appSettingsService.cookingSeconds();
        int deliveredAt = cookingUntil + appSettingsService.deliverySeconds();
        if (elapsed < accepted) {
            return OrderStatus.NEW;
        }
        if (elapsed < cookingUntil) {
            return OrderStatus.COOKING;
        }
        if (elapsed < deliveredAt) {
            return OrderStatus.ON_THE_WAY;
        }
        return OrderStatus.DELIVERED;
    }

    private static long elapsedSeconds(Order order) {
        if (order.getCreatedAt() == null) {
            return 0;
        }
        long seconds = Duration.between(order.getCreatedAt(), LocalDateTime.now()).getSeconds();
        return Math.max(0, seconds);
    }

    private double travelProgress(OrderStatus status, long elapsed) {
        if (status == OrderStatus.DELIVERED) {
            return 1;
        }
        if (status != OrderStatus.ON_THE_WAY) {
            return 0;
        }
        double span = deliveredAtSeconds() - cookingUntilSeconds();
        return Math.min(1, Math.max(0, (elapsed - cookingUntilSeconds()) / span));
    }

    private int progress(OrderStatus status, long elapsed, boolean waitingForCourier) {
        if (status == OrderStatus.CANCELLED) {
            return 0;
        }
        if (status == OrderStatus.DELIVERED) {
            return 100;
        }
        if (waitingForCourier) {
            int cookingShare = cookingUntilSeconds() * 100 / deliveredAtSeconds();
            return Math.min(cookingShare, (int) Math.min(99, elapsed * 100L / deliveredAtSeconds()));
        }
        return (int) Math.min(99, elapsed * 100L / deliveredAtSeconds());
    }

    private static String message(OrderStatus status, boolean waitingForCourier, boolean hasCourier) {
        if (waitingForCourier) {
            return "Заказ готовят. Ищем курьера для доставки";
        }
        return switch (status) {
            case NEW -> "Ресторан принял заказ и скоро начнёт готовку";
            case COOKING -> hasCourier
                    ? "Кухня готовит блюда, курьер едет или ждёт у ресторана"
                    : "Кухня готовит блюда, курьер ждёт у ресторана";
            case ON_THE_WAY -> "Курьер забрал заказ и едет к вам";
            case DELIVERED -> "Курьер передал заказ. Приятного аппетита";
            case CANCELLED -> "Заказ отменён";
        };
    }

    private static String paymentState(Order order) {
        if (order.getPaymentStatus() != null) {
            return order.getPaymentStatus().getLabel();
        }
        return Boolean.TRUE.equals(order.getPaid()) ? "Оплачено" : "К оплате при получении";
    }

    private static String paymentBadge(Order order) {
        if (order.getPaymentStatus() != null) {
            return order.getPaymentStatus().getBadge();
        }
        return Boolean.TRUE.equals(order.getPaid()) ? "success" : "secondary";
    }

    private static List<TrackingStepDto> steps(OrderStatus status) {
        OrderStatus[] flow = {OrderStatus.NEW, OrderStatus.COOKING, OrderStatus.ON_THE_WAY, OrderStatus.DELIVERED};
        String[] labels = {"Принят", "Готовится", "В пути", "Доставлен"};
        int current = status == OrderStatus.CANCELLED ? -1 : status.ordinal();
        List<TrackingStepDto> steps = new ArrayList<>();
        for (int i = 0; i < flow.length; i++) {
            String state;
            if (status == OrderStatus.DELIVERED || (status != OrderStatus.CANCELLED && i < current)) {
                state = "done";
            } else if (status != OrderStatus.CANCELLED && i == current) {
                state = "current";
            } else {
                state = "wait";
            }
            steps.add(new TrackingStepDto(labels[i], state));
        }
        return steps;
    }
}
