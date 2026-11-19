package univ.fooddeliv.utils;

public final class PaymentGateway {
    private PaymentGateway() {
    }

    public record Charge(boolean approved, String message, String brand, String last4) {
    }

    public static Charge charge(String cardNumber, String cvv) {
        String digits = cardNumber == null ? "" : cardNumber.replaceAll("\\D", "");
        if (digits.length() < 4) {
            return new Charge(false, "Банк не принял данные карты", null, null);
        }
        String last4 = digits.substring(digits.length() - 4);
        if ("000".equals(cvv)) {
            return new Charge(false, "Банк отклонил код безопасности", null, last4);
        }
        return switch (digits) {
            case "4000000000000002" -> new Charge(false, "Банк отклонил операцию", "Visa", last4);
            case "4000000000009995" -> new Charge(false, "Недостаточно средств на карте", "Visa", last4);
            case "4000000000000127" -> new Charge(false, "Карта заблокирована", "Visa", last4);
            default -> new Charge(true, "Оплата прошла", brand(digits), last4);
        };
    }

    public static String brand(String digits) {
        if (digits.startsWith("4")) {
            return "Visa";
        }
        if (digits.startsWith("5")) {
            return "Mastercard";
        }
        if (digits.startsWith("2200") || digits.startsWith("2201") || digits.startsWith("2202")
                || digits.startsWith("2203") || digits.startsWith("2204")) {
            return "Мир";
        }
        return "Карта";
    }
}
