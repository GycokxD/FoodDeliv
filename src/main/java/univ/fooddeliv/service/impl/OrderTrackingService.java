package univ.fooddeliv.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import univ.fooddeliv.dto.OrderTrackingDto;
import univ.fooddeliv.dto.TrackingStepDto;
import univ.fooddeliv.models.Order;
import univ.fooddeliv.models.OrderItem;
import univ.fooddeliv.models.OrderStatus;
import univ.fooddeliv.repository.OrderRepository;
import univ.fooddeliv.repository.UserRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderTrackingService {
    private static final String[] COURIERS = {"Азамат", "Нурлан", "Элина", "Бекзат"};

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final AppSettingsService appSettingsService;
    private final PaymentService paymentService;

    @Scheduled(fixedDelay = 3000)
    @Transactional
    public void advanceLiveOrders() {
        List<Order> live = orderRepository.findByLiveTrackingTrueAndStatusIn(
                List.of(OrderStatus.NEW, OrderStatus.COOKING, OrderStatus.ON_THE_WAY));
        live.forEach(this::advance);
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
        String restaurantName = "Ресторан";
        long restaurantSeed = order.getId() == null ? 1 : order.getId();
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                if (item.getDish() != null && item.getDish().getRestaurant() != null) {
                    restaurantName = item.getDish().getRestaurant().getName();
                    restaurantSeed = item.getDish().getRestaurant().getId();
                    break;
                }
            }
        }
        double restaurantX = 16 + unit(restaurantSeed * 3) * 22;
        double restaurantY = 22 + unit(restaurantSeed * 7) * 52;
        int addressSeed = order.getAddress() == null ? 17 : order.getAddress().hashCode();
        double destinationX = 62 + unit(addressSeed) * 22;
        double destinationY = 18 + unit(addressSeed * 13L) * 58;
        double travel = travelProgress(status, elapsed);
        double courierX = restaurantX + (destinationX - restaurantX) * travel;
        double courierY = restaurantY + (destinationY - restaurantY) * travel;
        int courierIndex = (int) (Math.floorMod(order.getId() == null ? 0 : order.getId(), COURIERS.length));

        return OrderTrackingDto.builder()
                .status(status.name())
                .statusLabel(status.getLabel())
                .badge(status.getBadge())
                .message(message(status))
                .eta(eta(status, elapsed))
                .progress(progress(status, elapsed))
                .live(Boolean.TRUE.equals(order.getLiveTracking()))
                .finished(delivered || cancelled)
                .canCancel(status == OrderStatus.NEW)
                .paid(Boolean.TRUE.equals(order.getPaid()))
                .paymentState(paymentState(order))
                .paymentBadge(paymentBadge(order))
                .courierName(COURIERS[courierIndex])
                .restaurantName(restaurantName)
                .restaurantX(round(restaurantX))
                .restaurantY(round(restaurantY))
                .destinationX(round(destinationX))
                .destinationY(round(destinationY))
                .courierX(round(courierX))
                .courierY(round(courierY))
                .showMap(!cancelled)
                .steps(steps(status))
                .build();
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

    private int progress(OrderStatus status, long elapsed) {
        if (status == OrderStatus.CANCELLED) {
            return 0;
        }
        if (status == OrderStatus.DELIVERED) {
            return 100;
        }
        return (int) Math.min(99, elapsed * 100 / deliveredAtSeconds());
    }

    private String eta(OrderStatus status, long elapsed) {
        if (status == OrderStatus.DELIVERED) {
            return "Заказ у вас";
        }
        if (status == OrderStatus.CANCELLED) {
            return "Доставка остановлена";
        }
        long left = Math.max(0, deliveredAtSeconds() - elapsed);
        if (left < 60) {
            return "меньше минуты";
        }
        long minutes = (left + 59) / 60;
        return "около " + minutes + " мин";
    }

    private static String message(OrderStatus status) {
        return switch (status) {
            case NEW -> "Ресторан принял заказ и скоро начнёт готовку";
            case COOKING -> "Кухня готовит блюда, курьер ждёт у ресторана";
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

    private static double unit(long seed) {
        long mixed = seed * 1103515245L + 12345L;
        return ((mixed >>> 16) & 0x7fff) / 32767.0;
    }

    private static double round(double value) {
        return Math.round(value * 10) / 10.0;
    }
}
