package univ.fooddeliv.dto;

import lombok.Value;

@Value
public class CourierOrderView {
    Long id;
    String statusLabel;
    String statusBadge;
    String restaurantName;
    String restaurantAddress;
    String address;
    String note;
    double totalPrice;
    Double distanceKm;
    String createdAt;
    String paymentLabel;
    boolean paid;
}
