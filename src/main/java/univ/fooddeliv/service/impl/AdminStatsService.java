package univ.fooddeliv.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import univ.fooddeliv.models.Order;
import univ.fooddeliv.models.OrderStatus;
import univ.fooddeliv.repository.DishRepository;
import univ.fooddeliv.repository.OrderItemRepository;
import univ.fooddeliv.repository.OrderRepository;
import univ.fooddeliv.repository.RestaurantRepository;
import univ.fooddeliv.repository.UserRepository;
import univ.fooddeliv.utils.DateFormatUtil;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminStatsService {
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("dd.MM");

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final DishRepository dishRepository;

    public Map<String, Object> dashboard() {
        List<Order> orders = orderRepository.findAllByOrderByCreatedAtDesc();
        double revenue = 0;
        long paidOrders = 0;
        EnumMap<OrderStatus, Long> byStatus = new EnumMap<>(OrderStatus.class);
        for (OrderStatus status : OrderStatus.values()) {
            byStatus.put(status, 0L);
        }
        for (Object[] row : orderRepository.countByStatus()) {
            byStatus.put((OrderStatus) row[0], ((Number) row[1]).longValue());
        }
        for (Order order : orders) {
            if (order.getStatus() != OrderStatus.CANCELLED) {
                revenue += order.getTotalPrice() == null ? 0 : order.getTotalPrice();
                paidOrders++;
            }
        }

        List<String> statusLabels = new ArrayList<>();
        List<Long> statusCounts = new ArrayList<>();
        List<Map<String, Object>> statusRows = new ArrayList<>();
        for (OrderStatus status : OrderStatus.values()) {
            statusLabels.add(status.getLabel());
            statusCounts.add(byStatus.get(status));
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("label", status.getLabel());
            row.put("count", byStatus.get(status));
            statusRows.add(row);
        }

        List<String> restaurantLabels = new ArrayList<>();
        List<Double> restaurantRevenue = new ArrayList<>();
        for (Object[] row : orderItemRepository.revenueByRestaurant(PageRequest.of(0, 8))) {
            restaurantLabels.add(String.valueOf(row[0]));
            restaurantRevenue.add(((Number) row[1]).doubleValue());
        }

        LocalDate start = LocalDate.now().minusDays(29);
        Map<LocalDate, long[]> days = new HashMap<>();
        for (int i = 0; i < 30; i++) {
            days.put(start.plusDays(i), new long[2]);
        }
        for (Order order : orders) {
            if (order.getCreatedAt() == null || order.getStatus() == OrderStatus.CANCELLED) {
                continue;
            }
            LocalDate date = order.getCreatedAt().toLocalDate();
            long[] bucket = days.get(date);
            if (bucket == null) {
                continue;
            }
            bucket[0]++;
            bucket[1] += Math.round(order.getTotalPrice() == null ? 0 : order.getTotalPrice());
        }
        List<String> dayLabels = new ArrayList<>();
        List<Long> dayOrders = new ArrayList<>();
        List<Long> dayRevenue = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            LocalDate date = start.plusDays(i);
            long[] bucket = days.get(date);
            dayLabels.add(date.format(DAY));
            dayOrders.add(bucket[0]);
            dayRevenue.add(bucket[1]);
        }

        List<Map<String, Object>> topDishes = new ArrayList<>();
        for (Object[] row : orderItemRepository.topDishes(PageRequest.of(0, 8))) {
            Map<String, Object> dish = new LinkedHashMap<>();
            dish.put("name", row[0]);
            dish.put("restaurant", row[1]);
            dish.put("quantity", ((Number) row[2]).longValue());
            dish.put("revenue", ((Number) row[3]).doubleValue());
            topDishes.add(dish);
        }

        List<Map<String, Object>> recent = new ArrayList<>();
        int limit = Math.min(8, orders.size());
        for (int i = 0; i < limit; i++) {
            Order order = orders.get(i);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", order.getId());
            row.put("customer", order.getUser() == null ? "—" : order.getUser().getName());
            row.put("status", order.getStatus() == null ? "—" : order.getStatus().getLabel());
            row.put("badge", order.getStatus() == null ? "secondary" : order.getStatus().getBadge());
            row.put("total", order.getTotalPrice());
            row.put("createdAt", DateFormatUtil.format(order.getCreatedAt()));
            recent.add(row);
        }

        Map<String, Object> model = new LinkedHashMap<>();
        model.put("userCount", userRepository.count());
        model.put("restaurantCount", restaurantRepository.count());
        model.put("dishCount", dishRepository.count());
        model.put("orderCount", orders.size());
        model.put("revenue", revenue);
        model.put("averageCheck", paidOrders == 0 ? 0 : revenue / paidOrders);
        model.put("statusLabels", statusLabels);
        model.put("statusCounts", statusCounts);
        model.put("statusRows", statusRows);
        model.put("restaurantLabels", restaurantLabels);
        model.put("restaurantRevenue", restaurantRevenue);
        model.put("dayLabels", dayLabels);
        model.put("dayOrders", dayOrders);
        model.put("dayRevenue", dayRevenue);
        model.put("topDishes", topDishes);
        model.put("recentOrders", recent);
        return model;
    }
}
