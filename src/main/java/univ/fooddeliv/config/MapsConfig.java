package univ.fooddeliv.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(MapsProperties.class)
public class MapsConfig {

    @Bean
    @Qualifier("nominatimClient")
    public RestClient nominatimClient(MapsProperties properties) {
        return client(properties, properties.nominatimUrl());
    }

    @Bean
    @Qualifier("osrmClient")
    public RestClient osrmClient(MapsProperties properties) {
        return client(properties, properties.osrmUrl());
    }

    private static RestClient client(MapsProperties properties, String baseUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(properties.timeoutMs()));
        factory.setReadTimeout(Duration.ofMillis(properties.timeoutMs()));
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .defaultHeader(HttpHeaders.USER_AGENT, properties.userAgent())
                .build();
    }
}
