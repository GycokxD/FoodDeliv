package univ.fooddeliv.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import univ.fooddeliv.models.AppSettings;

public interface AppSettingsRepository extends JpaRepository<AppSettings, Long> {
}
