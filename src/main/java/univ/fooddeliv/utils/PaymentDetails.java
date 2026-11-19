package univ.fooddeliv.utils;

import org.springframework.validation.BindingResult;
import univ.fooddeliv.dto.CheckoutDto;
import univ.fooddeliv.models.PaymentMethod;

import java.time.YearMonth;

public final class PaymentDetails {
    private PaymentDetails() {
    }

    public static void validate(CheckoutDto checkout, BindingResult bindingResult) {
        if (checkout.getPaymentMethod() == null) {
            bindingResult.rejectValue("paymentMethod", "payment", "Выберите способ оплаты");
            return;
        }
        if (checkout.getPaymentMethod() != PaymentMethod.CARD_ONLINE) {
            return;
        }
        String digits = digits(checkout.getCardNumber());
        if (digits.length() != 16 || !luhn(digits)) {
            bindingResult.rejectValue("cardNumber", "payment",
                    "Номер карты не проходит проверку. Для имитации используйте 4242 4242 4242 4242");
        }
        if (checkout.getCardHolder() == null || !checkout.getCardHolder().matches("^[A-Za-zА-Яа-яЁё][A-Za-zА-Яа-яЁё\\s\\-]{1,48}$")) {
            bindingResult.rejectValue("cardHolder", "payment", "Укажите имя владельца карты");
        }
        if (!validExpiry(checkout.getCardExpiry())) {
            bindingResult.rejectValue("cardExpiry", "payment", "Укажите срок действия карты, например 12/28");
        }
        if (checkout.getCardCvv() == null || !checkout.getCardCvv().matches("^[0-9]{3}$")) {
            bindingResult.rejectValue("cardCvv", "payment", "Укажите CVV из трёх цифр");
        }
    }

    public static String last4(String cardNumber) {
        String digits = digits(cardNumber);
        return digits.substring(digits.length() - 4);
    }

    private static boolean validExpiry(String expiry) {
        if (expiry == null || !expiry.matches("(0[1-9]|1[0-2])/[0-9]{2}")) {
            return false;
        }
        int month = Integer.parseInt(expiry.substring(0, 2));
        int year = 2000 + Integer.parseInt(expiry.substring(3));
        return !YearMonth.of(year, month).isBefore(YearMonth.now());
    }

    private static String digits(String cardNumber) {
        return cardNumber == null ? "" : cardNumber.replaceAll("\\D", "");
    }

    private static boolean luhn(String digits) {
        int sum = 0;
        boolean alternate = false;
        for (int i = digits.length() - 1; i >= 0; i--) {
            int digit = digits.charAt(i) - '0';
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            alternate = !alternate;
        }
        return sum % 10 == 0;
    }
}
