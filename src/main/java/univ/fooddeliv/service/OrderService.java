package univ.fooddeliv.service;

import univ.fooddeliv.dto.CheckoutDto;
import univ.fooddeliv.dto.DishDto;
import univ.fooddeliv.models.Order;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface OrderService {
    Order makeOrder(List<DishDto> dishesFromCookie, Authentication auth, CheckoutDto checkout);

    Double getTotalPrice(List<DishDto> dish);

    Order getOrderByIdForUser(Long orderId, String email);

    boolean cancelIfNew(Long orderId, String email);

    String toCartCookie(Long orderId, String email);
}
