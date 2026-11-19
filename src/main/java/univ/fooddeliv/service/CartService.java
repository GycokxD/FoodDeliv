package univ.fooddeliv.service;

import jakarta.servlet.http.Cookie;

public interface CartService {
    Cookie addDishToCookie(Cookie[] cookies, Long dishId);

    Cookie changeQuantityByDishId(Cookie[] cookies, Long dishId, boolean decrease);

    int countDishesInCart(Cookie[] cookies);

    Cookie deleteDishFromCookieById(Long dishId, Cookie[] cookies);

    Cookie setCart(String value);
}
