package univ.fooddeliv.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import univ.fooddeliv.models.PaymentMethod;

@Getter
@Setter
public class CheckoutDto {
    @NotBlank(message = "Укажите адрес доставки")
    @Size(max = 500, message = "Адрес слишком длинный")
    private String address;

    @NotBlank(message = "Укажите телефон")
    @Size(max = 30, message = "Телефон слишком длинный")
    @Pattern(regexp = "^[+0-9][0-9\\s\\-()]{6,19}$", message = "Укажите телефон, например +996555123456")
    private String phone;

    private Double latitude;
    private Double longitude;

    @Size(max = 500, message = "Комментарий слишком длинный")
    private String note;

    @NotNull(message = "Выберите способ оплаты")
    private PaymentMethod paymentMethod = PaymentMethod.CASH;

    private String cardNumber;
    private String cardHolder;
    private String cardExpiry;
    private String cardCvv;

    public void setAddress(String address) {
        this.address = blankToNull(address);
    }

    public void setPhone(String phone) {
        this.phone = blankToNull(phone);
    }

    public void setNote(String note) {
        this.note = blankToNull(note);
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = blankToNull(cardNumber);
    }

    public void setCardHolder(String cardHolder) {
        this.cardHolder = blankToNull(cardHolder);
    }

    public void setCardExpiry(String cardExpiry) {
        this.cardExpiry = blankToNull(cardExpiry);
    }

    public void setCardCvv(String cardCvv) {
        this.cardCvv = blankToNull(cardCvv);
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
