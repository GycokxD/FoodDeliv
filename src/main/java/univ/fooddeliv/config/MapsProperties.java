package univ.fooddeliv.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "maps")
public record MapsProperties(
        String nominatimUrl,
        String osrmUrl,
        String osrmProfile,
        String userAgent,
        String countryCodes,
        double centerLat,
        double centerLng,
        double searchRadiusDeg,
        int timeoutMs
) {
}
