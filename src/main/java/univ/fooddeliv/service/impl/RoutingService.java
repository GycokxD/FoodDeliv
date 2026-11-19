package univ.fooddeliv.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import univ.fooddeliv.config.MapsProperties;
import univ.fooddeliv.dto.RouteDto;
import univ.fooddeliv.utils.GeoUtil;
import univ.fooddeliv.utils.LruCache;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
public class RoutingService {
    private static final double FALLBACK_SPEED_MPS = 25_000 / 3600.0;

    private final RestClient osrm;
    private final MapsProperties properties;
    private final LruCache<String, RouteDto> cache = new LruCache<>(500);

    public RoutingService(@Qualifier("osrmClient") RestClient osrm, MapsProperties properties) {
        this.osrm = osrm;
        this.properties = properties;
    }

    public RouteDto route(double fromLat, double fromLng, double toLat, double toLng) {
        String key = String.format(Locale.ROOT, "%.4f,%.4f;%.4f,%.4f", fromLat, fromLng, toLat, toLng);
        RouteDto cached = cache.get(key);
        if (cached != null) {
            return cached;
        }
        String path = String.format(Locale.ROOT, "/route/v1/%s/%.6f,%.6f;%.6f,%.6f",
                properties.osrmProfile(), fromLng, fromLat, toLng, toLat);
        try {
            Map<String, Object> body = osrm.get()
                    .uri(uri -> uri.path(path)
                            .queryParam("overview", "full")
                            .queryParam("geometries", "geojson")
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {
                    });
            RouteDto route = parse(body);
            if (route != null) {
                cache.put(key, route);
                return route;
            }
        } catch (RestClientException e) {
            log.warn("OSRM route failed for {}: {}", key, e.getMessage());
        }
        return straightLine(fromLat, fromLng, toLat, toLng);
    }

    @SuppressWarnings("unchecked")
    private static RouteDto parse(Map<String, Object> body) {
        if (body == null || !"Ok".equals(body.get("code"))) {
            return null;
        }
        List<Map<String, Object>> routes = (List<Map<String, Object>>) body.get("routes");
        if (routes == null || routes.isEmpty()) {
            return null;
        }
        Map<String, Object> best = routes.get(0);
        Map<String, Object> geometry = (Map<String, Object>) best.get("geometry");
        List<List<Number>> coordinates = geometry == null ? null : (List<List<Number>>) geometry.get("coordinates");
        if (coordinates == null || coordinates.size() < 2) {
            return null;
        }
        List<double[]> points = new ArrayList<>(coordinates.size());
        for (List<Number> pair : coordinates) {
            points.add(new double[]{pair.get(1).doubleValue(), pair.get(0).doubleValue()});
        }
        double distance = ((Number) best.getOrDefault("distance", 0)).doubleValue();
        double duration = ((Number) best.getOrDefault("duration", 0)).doubleValue();
        return new RouteDto(List.copyOf(points), distance, duration, false);
    }

    private static RouteDto straightLine(double fromLat, double fromLng, double toLat, double toLng) {
        double distance = GeoUtil.distanceMeters(fromLat, fromLng, toLat, toLng);
        List<double[]> points = List.of(new double[]{fromLat, fromLng}, new double[]{toLat, toLng});
        return new RouteDto(points, distance, distance / FALLBACK_SPEED_MPS, true);
    }
}
