package univ.fooddeliv.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthDto {
    @NotBlank(message = "Укажите email")
    @Email(message = "Некорректный email")
    private String email;

    @NotBlank(message = "Укажите пароль")
    @Size(min = 6, message = "Пароль должен быть не короче 6 символов")
    private String password;

    @NotBlank(message = "Укажите имя")
    private String name;

    @NotBlank(message = "Подтвердите пароль")
    private String confirmPassword;
}
