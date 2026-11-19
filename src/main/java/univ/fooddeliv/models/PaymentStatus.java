package univ.fooddeliv.models;

import lombok.Getter;

@Getter
public enum PaymentStatus {
    UNPAID("К оплате при получении", "secondary"),
    PAID("Оплачено", "success"),
    REFUNDED("Возврат на карту", "warning");

    private final String label;
    private final String badge;

    PaymentStatus(String label, String badge) {
        this.label = label;
        this.badge = badge;
    }
}
