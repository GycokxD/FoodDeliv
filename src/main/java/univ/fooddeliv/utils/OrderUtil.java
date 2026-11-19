package univ.fooddeliv.utils;

import univ.fooddeliv.models.Order;
import univ.fooddeliv.models.OrderItem;
import univ.fooddeliv.models.Restaurant;

import java.util.Optional;

public final class OrderUtil {
    private OrderUtil() {
    }

    public static Optional<Restaurant> restaurantOf(Order order) {
        if (order.getItems() == null) {
            return Optional.empty();
        }
        for (OrderItem item : order.getItems()) {
            if (item.getDish() != null && item.getDish().getRestaurant() != null) {
                return Optional.of(item.getDish().getRestaurant());
            }
        }
        return Optional.empty();
    }

    public static Optional<double[]> restaurantPoint(Order order) {
        return restaurantOf(order)
                .filter(restaurant -> GeoUtil.isValid(restaurant.getLatitude(), restaurant.getLongitude()))
                .map(restaurant -> new double[]{restaurant.getLatitude(), restaurant.getLongitude()});
    }

    public static Optional<double[]> destinationPoint(Order order) {
        return GeoUtil.isValid(order.getLatitude(), order.getLongitude())
                ? Optional.of(new double[]{order.getLatitude(), order.getLongitude()})
                : Optional.empty();
    }

    public static Optional<double[]> courierPoint(Order order) {
        return GeoUtil.isValid(order.getCourierLatitude(), order.getCourierLongitude())
                ? Optional.of(new double[]{order.getCourierLatitude(), order.getCourierLongitude()})
                : Optional.empty();
    }
}
