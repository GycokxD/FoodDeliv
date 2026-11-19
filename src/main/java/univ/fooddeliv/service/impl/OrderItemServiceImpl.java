package univ.fooddeliv.service.impl;

import univ.fooddeliv.dto.DishDto;
import univ.fooddeliv.models.Order;
import univ.fooddeliv.models.OrderItem;
import univ.fooddeliv.repository.OrderItemRepository;
import univ.fooddeliv.service.DishService;
import univ.fooddeliv.service.OrderItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderItemServiceImpl implements OrderItemService {
    private final OrderItemRepository orderItemRepository;
    private final DishService dishService;

    @Override
    public void createOrderItem(DishDto dish, Order createdOrder) {
        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(createdOrder);
        orderItem.setDish(dishService.findDishById(dish.getId()));
        orderItem.setQuantity(dish.getQuantity());
        orderItem.setPrice(dish.getTotalPrice());
        orderItemRepository.save(orderItem);
    }
}
