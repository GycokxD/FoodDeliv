package univ.fooddeliv.controllers;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import univ.fooddeliv.dto.DishDto;
import univ.fooddeliv.dto.RestaurantDto;
import univ.fooddeliv.dto.SearchHitDto;
import univ.fooddeliv.models.Dish;
import univ.fooddeliv.models.User;
import univ.fooddeliv.service.CartService;
import univ.fooddeliv.service.DishService;
import univ.fooddeliv.service.RestaurantService;
import univ.fooddeliv.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/restaurants")
@RequiredArgsConstructor
public class RestaurantController {
    private final CartService cartService;
    private final RestaurantService restaurantService;
    private final DishService dishService;
    private final UserService userService;

    @GetMapping
    public String getAllRestaurants(@RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "6") int size,
                                    @AuthenticationPrincipal UserDetails userDetails,
                                    Model model,
                                    HttpServletRequest request) {
        Page<RestaurantDto> restaurants = restaurantService.getAllRestaurants(page, size);
        if (userDetails != null) {
            User user = userService.findByEmail(userDetails.getUsername());
            model.addAttribute("user", user);
        }
        model.addAttribute("cartCount", cartService.countDishesInCart(request.getCookies()));

        model.addAttribute("restaurants", restaurants);
        return "restaurant/list";
    }

    @GetMapping("/search")
    @ResponseBody
    public List<SearchHitDto> search(@RequestParam(defaultValue = "") String q) {
        return restaurantService.search(q);
    }

    @GetMapping("/{id:\\d+}/dishes/search")
    @ResponseBody
    public List<DishDto> searchDishes(@PathVariable Long id,
                                     @RequestParam(defaultValue = "") String q,
                                     @RequestParam(required = false) Double minPrice,
                                     @RequestParam(required = false) Double maxPrice) {
        return dishService.searchMenu(id, q, minPrice, maxPrice);
    }

    @GetMapping("/{id:\\d+}")
    public String getRestaurantById(@PathVariable Long id,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "12") int size,
                                    @RequestParam(defaultValue = "name") String sort,
                                    @RequestParam(required = false) Double minPrice,
                                    @RequestParam(required = false) Double maxPrice,
                                    @AuthenticationPrincipal UserDetails userDetails,
                                    Model model,
                                    HttpServletRequest request) {
        if (!"price_asc".equals(sort) && !"price_desc".equals(sort)) {
            sort = "name";
        }
        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            Double swap = minPrice;
            minPrice = maxPrice;
            maxPrice = swap;
        }
        RestaurantDto restaurant = restaurantService.getRestaurantById(id);
        Page<DishDto> dishes = dishService.getMenu(id, page, size, sort, minPrice, maxPrice);
        StringBuilder menuQuery = new StringBuilder("sort=").append(sort);
        if (minPrice != null) {
            menuQuery.append("&minPrice=").append(minPrice);
        }
        if (maxPrice != null) {
            menuQuery.append("&maxPrice=").append(maxPrice);
        }
        model.addAttribute("sort", sort);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("menuQuery", menuQuery.toString());
        if (userDetails != null) {
            User user = userService.findByEmail(userDetails.getUsername());
            model.addAttribute("user", user);
        }
        model.addAttribute("cartCount", cartService.countDishesInCart(request.getCookies()));


        model.addAttribute("restaurant", restaurant);
        model.addAttribute("dishes", dishes);

        return "restaurant/details";
    }
    @PostMapping("add-dish-to-cart")
    public String addDish(@RequestParam Long dishId,
                          @RequestParam(defaultValue = "false") boolean replace,
                          HttpServletRequest httpServletRequest,
                          HttpServletResponse httpServletResponse,
                          RedirectAttributes redirectAttributes) {
        Dish dish = dishService.findDishById(dishId);
        if (dish == null || dish.getRestaurant() == null) {
            redirectAttributes.addFlashAttribute("message", "Блюдо не найдено");
            redirectAttributes.addFlashAttribute("messageType", "danger");
            return "redirect:/restaurants";
        }
        Long restaurantId = dish.getRestaurant().getId();
        List<DishDto> inCart = dishService.getDishesFromCookie(httpServletRequest.getCookies());
        boolean conflict = false;
        if (inCart != null) {
            for (DishDto item : inCart) {
                if (item.getRestaurantId() != null && !item.getRestaurantId().equals(restaurantId)) {
                    conflict = true;
                    break;
                }
            }
        }
        if (conflict && !replace) {
            redirectAttributes.addFlashAttribute("cartConflict", true);
            redirectAttributes.addFlashAttribute("conflictDishId", dishId);
            redirectAttributes.addFlashAttribute("conflictDishName", dish.getName());
            redirectAttributes.addFlashAttribute("conflictRestaurantId", restaurantId);
            return "redirect:/restaurants/" + restaurantId;
        }
        Cookie cookie = conflict
                ? cartService.setCart(dishId + "#1!")
                : cartService.addDishToCookie(httpServletRequest.getCookies(), dishId);
        httpServletResponse.addCookie(cookie);
        redirectAttributes.addFlashAttribute("message",
                conflict ? "Корзина очищена, блюдо добавлено" : "Блюдо добавлено в корзину");
        redirectAttributes.addFlashAttribute("messageType", "success");
        return "redirect:/restaurants/" + restaurantId;
    }


}
