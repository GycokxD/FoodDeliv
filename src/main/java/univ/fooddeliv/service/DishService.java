package univ.fooddeliv.service;

import jakarta.servlet.http.Cookie;
import univ.fooddeliv.dto.DishDto;
import univ.fooddeliv.models.Dish;
import org.springframework.data.domain.Page;

import java.util.List;

public interface DishService {
    Page<DishDto> getDishesByRestaurantId(Long restaurantId, int page, int size);

    Page<DishDto> getMenu(Long restaurantId, int page, int size, String sort, Double minPrice, Double maxPrice);

    List<DishDto> searchByRestaurant(Long restaurantId, String query);

    List<DishDto> searchMenu(Long restaurantId, String query, Double minPrice, Double maxPrice);

    List<DishDto> getDishesFromCookie(Cookie[] cookie);

    Dish findDishById(Long id);
}
