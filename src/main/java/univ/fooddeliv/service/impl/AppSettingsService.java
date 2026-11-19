package univ.fooddeliv.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import univ.fooddeliv.models.AppSettings;
import univ.fooddeliv.repository.AppSettingsRepository;

@Service
@RequiredArgsConstructor
public class AppSettingsService {
    public static final long SETTINGS_ID = 1L;

    private final AppSettingsRepository appSettingsRepository;

    @Transactional(readOnly = true)
    public AppSettings get() {
        return appSettingsRepository.findById(SETTINGS_ID).orElseGet(this::defaults);
    }

    @Transactional
    public AppSettings update(int acceptedSeconds, int cookingSeconds, int deliverySeconds, String supportPhone, String city) {
        if (acceptedSeconds < 5 || acceptedSeconds > 1800
                || cookingSeconds < 5 || cookingSeconds > 1800
                || deliverySeconds < 5 || deliverySeconds > 1800) {
            throw new IllegalArgumentException("Каждый этап доставки должен длиться от 5 до 1800 секунд");
        }
        AppSettings settings = appSettingsRepository.findById(SETTINGS_ID).orElseGet(this::defaults);
        settings.setId(SETTINGS_ID);
        settings.setAcceptedSeconds(acceptedSeconds);
        settings.setCookingSeconds(cookingSeconds);
        settings.setDeliverySeconds(deliverySeconds);
        settings.setSupportPhone(blankToNull(supportPhone));
        settings.setCity(blankToNull(city));
        return appSettingsRepository.save(settings);
    }

    public int acceptedSeconds() {
        return positive(get().getAcceptedSeconds(), 20);
    }

    public int cookingSeconds() {
        return positive(get().getCookingSeconds(), 30);
    }

    public int deliverySeconds() {
        return positive(get().getDeliverySeconds(), 60);
    }

    public boolean demoCouriers() {
        return !Boolean.FALSE.equals(get().getDemoCouriers());
    }

    private AppSettings defaults() {
        AppSettings settings = new AppSettings();
        settings.setId(SETTINGS_ID);
        settings.setDemoCouriers(true);
        settings.setAcceptedSeconds(20);
        settings.setCookingSeconds(30);
        settings.setDeliverySeconds(60);
        settings.setSupportPhone("+996700000000");
        settings.setCity("Бишкек");
        return settings;
    }

    private static int positive(Integer value, int fallback) {
        return value == null || value < 5 ? fallback : value;
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
