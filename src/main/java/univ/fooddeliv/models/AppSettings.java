package univ.fooddeliv.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "app_settings")
public class AppSettings {
    @Id
    private Long id;

    @Column(name = "accepted_seconds", nullable = false)
    private Integer acceptedSeconds;

    @Column(name = "cooking_seconds", nullable = false)
    private Integer cookingSeconds;

    @Column(name = "delivery_seconds", nullable = false)
    private Integer deliverySeconds;

    @Column(name = "support_phone")
    private String supportPhone;

    @Column(name = "city")
    private String city;
}
