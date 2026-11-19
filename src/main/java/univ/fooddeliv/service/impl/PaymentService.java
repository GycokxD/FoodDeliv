package univ.fooddeliv.service.impl;

import org.springframework.stereotype.Service;
import univ.fooddeliv.dto.CheckoutDto;
import univ.fooddeliv.exceptions.PaymentDeclinedException;
import univ.fooddeliv.models.Order;
import univ.fooddeliv.models.PaymentMethod;
import univ.fooddeliv.models.PaymentStatus;
import univ.fooddeliv.utils.PaymentGateway;

import java.time.LocalDateTime;

@Service
public class PaymentService {

    public void applyCheckout(Order order, CheckoutDto checkout) {
        order.setPaymentMethod(checkout.getPaymentMethod());
        if (checkout.getPaymentMethod() == PaymentMethod.CARD_ONLINE) {
            PaymentGateway.Charge charge = PaymentGateway.charge(checkout.getCardNumber(), checkout.getCardCvv());
            if (!charge.approved()) {
                throw new PaymentDeclinedException(charge.message());
            }
            markPaid(order, charge.brand(), charge.last4());
            return;
        }
        order.setPaid(false);
        order.setPaymentStatus(PaymentStatus.UNPAID);
        order.setCardLast4(null);
        order.setCardBrand(null);
        order.setPaidAt(null);
    }

    public void captureOnDelivery(Order order) {
        if (order.getPaymentStatus() == PaymentStatus.REFUNDED || Boolean.TRUE.equals(order.getPaid())) {
            return;
        }
        markPaid(order, order.getCardBrand(), order.getCardLast4());
    }

    public boolean refundOnline(Order order) {
        if (order.getPaymentMethod() != PaymentMethod.CARD_ONLINE || order.getPaymentStatus() != PaymentStatus.PAID) {
            return false;
        }
        order.setPaymentStatus(PaymentStatus.REFUNDED);
        order.setPaid(false);
        return true;
    }

    public void assignReceipt(Order order) {
        if (order.getId() == null || order.getPaymentStatus() != PaymentStatus.PAID || order.getPaymentRef() != null) {
            return;
        }
        int year = order.getCreatedAt() == null ? LocalDateTime.now().getYear() : order.getCreatedAt().getYear();
        order.setPaymentRef(String.format("EF-%d-%06d", year, order.getId()));
    }

    private static void markPaid(Order order, String brand, String last4) {
        order.setPaid(true);
        order.setPaymentStatus(PaymentStatus.PAID);
        order.setCardBrand(brand);
        order.setCardLast4(last4);
        if (order.getPaidAt() == null) {
            order.setPaidAt(LocalDateTime.now());
        }
    }
}
