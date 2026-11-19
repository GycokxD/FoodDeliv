package univ.fooddeliv.models;

import lombok.Getter;

@Getter
public enum OrderStatus {
    NEW("Новый", "primary"),
    COOKING("Готовится", "warning text-dark"),
    ON_THE_WAY("В пути", "info text-dark"),
    DELIVERED("Доставлен", "success"),
    CANCELLED("Отменён", "secondary");

    private final String label;
    private final String badge;

    OrderStatus(String label, String badge) {
        this.label = label;
        this.badge = badge;
    }
}
