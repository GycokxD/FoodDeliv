package univ.fooddeliv.dto;

import java.util.List;

/**
 * Маршрут по дорогам. {@code points} — пары [широта, долгота] в порядке движения.
 * {@code approximate} = true, если сервис маршрутов недоступен и это прямая линия.
 */
public record RouteDto(List<double[]> points, double distanceMeters, double durationSeconds, boolean approximate) {
}
