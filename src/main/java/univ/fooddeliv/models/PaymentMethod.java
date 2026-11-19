package univ.fooddeliv.models;

import lombok.Getter;

@Getter
public enum PaymentMethod {
    CASH("Наличными курьеру"),
    CARD_COURIER("Картой курьеру"),
    CARD_ONLINE("Картой онлайн");

    private final String label;

    PaymentMethod(String label) {
        this.label = label;
    }
}
