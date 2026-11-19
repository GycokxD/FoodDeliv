package univ.fooddeliv.service.impl;

import jakarta.servlet.http.Cookie;
import univ.fooddeliv.dto.DishDto;
import univ.fooddeliv.service.CartService;
import univ.fooddeliv.service.DishService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    private final DishService dishService;


    @Override
    public Cookie addDishToCookie(Cookie[] cookies, Long dishId) {
        String cookieStr = getCartValue(cookies);
        StringBuilder sb = new StringBuilder();
        boolean found = false;

        if (!cookieStr.isBlank()) {
            for (String entry : cookieStr.split("!")) {
                if (entry.isBlank()) {
                    continue;
                }
                String[] parts = entry.split("#");
                Long id = Long.valueOf(parts[0]);
                long quantity = Long.parseLong(parts[1]);
                if (id.equals(dishId)) {
                    quantity++;
                    found = true;
                }
                sb.append(id).append("#").append(quantity).append("!");
            }
        }

        if (!found) {
            sb.append(dishId).append("#1!");
        }

        return buildCartCookie(sb.toString());
    }

    @Override
    public Cookie changeQuantityByDishId(Cookie[] cookies, Long dishId, boolean decrease) {
        String dishes = getCartValue(cookies);
        if (dishes.isBlank()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (String entry : dishes.split("!")) {
            if (entry.isBlank()) {
                continue;
            }
            String[] parts = entry.split("#");
            Long dishIdInCart = Long.valueOf(parts[0]);
            long quantity = Long.parseLong(parts[1]);

            if (dishId.equals(dishIdInCart)) {
                if (decrease) {
                    quantity--;
                } else {
                    quantity++;
                }
                if (quantity <= 0) {
                    continue;
                }
            }
            sb.append(dishIdInCart).append("#").append(quantity).append("!");
        }
        return buildCartCookie(sb.toString());
    }
    @Override
    public int countDishesInCart(Cookie[] cookies) {
        if (cookies != null) {
            List<DishDto> dishDtos = dishService.getDishesFromCookie(cookies);
            if (dishDtos != null) {
                return dishDtos.size();
            }
            return 0;
        }
        return 0;
    }

    @Override
    public Cookie deleteDishFromCookieById(Long dishId, Cookie[] cookies) {
        String cartValue = getCartValue(cookies);
        if (cartValue.isBlank()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (String entry : cartValue.split("!")) {
            if (entry.isBlank()) {
                continue;
            }
            Long dishIdInCart = Long.valueOf(entry.split("#")[0]);
            if (!dishId.equals(dishIdInCart)) {
                sb.append(entry).append("!");
            }
        }
        return buildCartCookie(sb.toString());
    }

    @Override
    public Cookie setCart(String value) {
        return buildCartCookie(value == null ? "" : value);
    }

    private String getCartValue(Cookie[] cookies) {
        if (cookies == null) {
            return "";
        }
        for (Cookie cookie : cookies) {
            if ("cart".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return "";
    }

    private Cookie buildCartCookie(String value) {
        Cookie cookie = new Cookie("cart", value);
        cookie.setMaxAge(7 * 24 * 60 * 60);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        return cookie;
    }

}
