package univ.fooddeliv.service;

import univ.fooddeliv.dto.DishDto;
import univ.fooddeliv.models.Order;

public interface OrderItemService {
    void createOrderItem(DishDto dish, Order createdOrder);
}
