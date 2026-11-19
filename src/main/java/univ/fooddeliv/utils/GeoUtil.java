package univ.fooddeliv.utils;

import java.util.List;

public final class GeoUtil {
    private static final double EARTH_RADIUS_METERS = 6_371_000;

    private GeoUtil() {
    }

    public static boolean isValid(Double lat, Double lng) {
        return lat != null && lng != null
                && !lat.isNaN() && !lng.isNaN()
                && lat >= -90 && lat <= 90
                && lng >= -180 && lng <= 180;
    }

    public static double distanceMeters(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return 2 * EARTH_RADIUS_METERS * Math.asin(Math.min(1, Math.sqrt(a)));
    }

    public static double lengthMeters(List<double[]> points) {
        double total = 0;
        for (int i = 1; i < points.size(); i++) {
            total += distanceMeters(points.get(i - 1)[0], points.get(i - 1)[1], points.get(i)[0], points.get(i)[1]);
        }
        return total;
    }

    /**
     * Точка на ломаной, пройденной на долю {@code fraction} (0..1) от её длины.
     */
    public static double[] pointAlong(List<double[]> points, double fraction) {
        if (points.isEmpty()) {
            throw new IllegalArgumentException("Маршрут пуст");
        }
        if (points.size() == 1 || fraction <= 0) {
            return points.get(0);
        }
        if (fraction >= 1) {
            return points.get(points.size() - 1);
        }
        double target = lengthMeters(points) * fraction;
        double walked = 0;
        for (int i = 1; i < points.size(); i++) {
            double[] from = points.get(i - 1);
            double[] to = points.get(i);
            double segment = distanceMeters(from[0], from[1], to[0], to[1]);
            if (walked + segment >= target) {
                double t = segment == 0 ? 0 : (target - walked) / segment;
                return new double[]{from[0] + (to[0] - from[0]) * t, from[1] + (to[1] - from[1]) * t};
            }
            walked += segment;
        }
        return points.get(points.size() - 1);
    }
}
