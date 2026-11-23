package univ.fooddeliv.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class OrderTrackingDto {
    private final String status;
    private final String statusLabel;
    private final String badge;
    private final String message;
    private final String eta;
    private final int progress;
    private final boolean live;
    private final boolean finished;
    private final boolean canCancel;
    private final boolean paid;
    private final String paymentState;
    private final String paymentBadge;
    private final String courierName;
    private final String restaurantName;
    private final Double restaurantLat;
    private final Double restaurantLng;
    private final Double destinationLat;
    private final Double destinationLng;
    private final Double courierLat;
    private final Double courierLng;
    private final List<double[]> route;
    private final double centerLat;
    private final double centerLng;
    private final boolean showMap;
    private final boolean waitingForCourier;
    private final List<TrackingStepDto> steps;
}
