package univ.fooddeliv.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import univ.fooddeliv.config.MapsProperties;
import univ.fooddeliv.dto.GeoAddressDto;
import univ.fooddeliv.utils.GeoUtil;
import univ.fooddeliv.utils.LruCache;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
public class GeocodingService {
    private static final long MIN_INTERVAL_MS = 1100;
    private static final int MIN_QUERY_LENGTH = 3;
    private static final int MAX_QUERY_LENGTH = 200;

    private final RestClient nominatim;
    private final MapsProperties properties;
    private final LruCache<String, List<GeoAddressDto>> searchCache = new LruCache<>(500);
    private final LruCache<String, GeoAddressDto> reverseCache = new LruCache<>(1000);
    private long nextRequestAt = 0;

    public GeocodingService(@Qualifier("nominatimClient") RestClient nominatim, MapsProperties properties) {
        this.nominatim = nominatim;
        this.properties = properties;
    }

    public List<GeoAddressDto> search(String query) {
        String clean = query == null ? "" : query.trim().replaceAll("\\s+", " ");
        if (clean.length() < MIN_QUERY_LENGTH) {
            return List.of();
        }
        if (clean.length() > MAX_QUERY_LENGTH) {
            clean = clean.substring(0, MAX_QUERY_LENGTH);
        }
        String key = clean.toLowerCase(Locale.ROOT);
        List<GeoAddressDto> cached = searchCache.get(key);
        if (cached != null) {
            return cached;
        }
        String q = clean;
        double r = properties.searchRadiusDeg();
        String viewbox = String.format(Locale.ROOT, "%.5f,%.5f,%.5f,%.5f",
                properties.centerLng() - r, properties.centerLat() + r,
                properties.centerLng() + r, properties.centerLat() - r);
        try {
            awaitSlot();
            List<Map<String, Object>> hits = nominatim.get()
                    .uri(uri -> uri.path("/search")
                            .queryParam("q", q)
                            .queryParam("format", "jsonv2")
                            .queryParam("addressdetails", 1)
                            .queryParam("limit", 6)
                            .queryParam("countrycodes", properties.countryCodes())
                            .queryParam("viewbox", viewbox)
                            .queryParam("bounded", 1)
                            .queryParam("accept-language", "ru")
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {
                    });
            List<GeoAddressDto> result = new ArrayList<>();
            Set<String> labels = new HashSet<>();
            if (hits != null) {
                for (Map<String, Object> hit : hits) {
                    toAddress(hit)
                            .filter(address -> labels.add(address.label().toLowerCase(Locale.ROOT)))
                            .ifPresent(result::add);
                }
            }
            searchCache.put(key, List.copyOf(result));
            return result;
        } catch (RestClientException e) {
            log.warn("Nominatim search failed for '{}': {}", q, e.getMessage());
            return List.of();
        }
    }

    public GeoAddressDto reverse(double lat, double lng) {
        String key = String.format(Locale.ROOT, "%.5f,%.5f", lat, lng);
        GeoAddressDto cached = reverseCache.get(key);
        if (cached != null) {
            return cached;
        }
        GeoAddressDto fallback = new GeoAddressDto(String.format(Locale.ROOT, "%.5f, %.5f", lat, lng), lat, lng);
        try {
            awaitSlot();
            Map<String, Object> hit = nominatim.get()
                    .uri(uri -> uri.path("/reverse")
                            .queryParam("lat", lat)
                            .queryParam("lon", lng)
                            .queryParam("format", "jsonv2")
                            .queryParam("addressdetails", 1)
                            .queryParam("zoom", 18)
                            .queryParam("accept-language", "ru")
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {
                    });
            if (hit == null || hit.containsKey("error")) {
                return fallback;
            }
            String label = labelOf(hit);
            GeoAddressDto result = new GeoAddressDto(label == null ? fallback.label() : label, lat, lng);
            reverseCache.put(key, result);
            return result;
        } catch (RestClientException e) {
            log.warn("Nominatim reverse failed for {}: {}", key, e.getMessage());
            return fallback;
        }
    }

    public Optional<GeoAddressDto> geocode(String address) {
        return search(address).stream().findFirst();
    }

    private synchronized void awaitSlot() {
        long now = System.currentTimeMillis();
        long wait = nextRequestAt - now;
        if (wait > 0) {
            try {
                Thread.sleep(wait);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        nextRequestAt = Math.max(now, nextRequestAt) + MIN_INTERVAL_MS;
    }

    private static Optional<GeoAddressDto> toAddress(Map<String, Object> hit) {
        try {
            double lat = Double.parseDouble(String.valueOf(hit.get("lat")));
            double lng = Double.parseDouble(String.valueOf(hit.get("lon")));
            if (!GeoUtil.isValid(lat, lng)) {
                return Optional.empty();
            }
            String label = labelOf(hit);
            return label == null ? Optional.empty() : Optional.of(new GeoAddressDto(label, lat, lng));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    private static String labelOf(Map<String, Object> hit) {
        Object raw = hit.get("address");
        if (raw instanceof Map<?, ?> map) {
            Map<String, Object> address = (Map<String, Object>) map;
            String street = first(address, "road", "pedestrian", "footway", "neighbourhood", "suburb");
            String house = first(address, "house_number");
            String place = first(address, "amenity", "shop", "building");
            String city = first(address, "city", "town", "village");
            StringBuilder label = new StringBuilder();
            if (street != null) {
                label.append(street);
                if (house != null) {
                    label.append(", ").append(house);
                }
            } else if (place != null) {
                label.append(place);
            }
            if (city != null) {
                label.append(label.isEmpty() ? "" : ", ").append(city);
            }
            if (!label.isEmpty()) {
                return label.toString();
            }
        }
        Object display = hit.get("display_name");
        return display == null ? null : String.valueOf(display);
    }

    private static String first(Map<String, Object> address, String... keys) {
        for (String key : keys) {
            Object value = address.get(key);
            if (value != null && !String.valueOf(value).isBlank()) {
                return String.valueOf(value);
            }
        }
        return null;
    }
}
