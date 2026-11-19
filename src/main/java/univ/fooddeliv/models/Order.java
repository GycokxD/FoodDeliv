package univ.fooddeliv.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @JoinColumn(name = "user_id")
    @ManyToOne
    private User user;
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "total_price")
    private Double totalPrice;
    @Column(name = "address")
    private String address;
    @Column(name = "phone")
    private String phone;
    @Column(name = "note")
    private String note;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status = OrderStatus.NEW;
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;
    @Column(name = "paid", nullable = false)
    private Boolean paid = false;
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;
    @Column(name = "payment_ref")
    private String paymentRef;
    @Column(name = "card_brand")
    private String cardBrand;
    @Column(name = "card_last4")
    private String cardLast4;
    @Column(name = "paid_at")
    private LocalDateTime paidAt;
    @Column(name = "live_tracking", nullable = false)
    private Boolean liveTracking = false;
    @Column(name = "latitude")
    private Double latitude;
    @Column(name = "longitude")
    private Double longitude;
    @ManyToOne
    @JoinColumn(name = "courier_id")
    private User courier;
    @Column(name = "courier_latitude")
    private Double courierLatitude;
    @Column(name = "courier_longitude")
    private Double courierLongitude;
    @Column(name = "courier_located_at")
    private LocalDateTime courierLocatedAt;

    @OneToMany(mappedBy = "order")
    private List<OrderItem> items;
}
