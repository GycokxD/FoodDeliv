package univ.fooddeliv.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileUpdateDto {
    @NotBlank(message = "Укажите имя")
    @Size(max = 255, message = "Имя слишком длинное")
    private String name;

    @Size(max = 30, message = "Телефон слишком длинный")
    @Pattern(regexp = "^[+0-9][0-9\\s\\-()]{6,19}$", message = "Укажите телефон, например +996555123456")
    private String phone;

    private String currentPassword;
    private String newPassword;
    private String confirmPassword;

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public void setPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            this.phone = null;
            return;
        }
        this.phone = phone.trim();
    }
}
