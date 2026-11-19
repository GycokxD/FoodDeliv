package univ.fooddeliv.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import univ.fooddeliv.dto.CheckoutDto;
import univ.fooddeliv.dto.DishDto;
import univ.fooddeliv.models.User;
import univ.fooddeliv.service.CartService;
import univ.fooddeliv.service.DishService;
import univ.fooddeliv.service.OrderService;
import univ.fooddeliv.repository.OrderRepository;
import univ.fooddeliv.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("cart")
@RequiredArgsConstructor
public class CartController {
    private final DishService dishService;
    private final CartService cartService;
    private final OrderService orderService;
    private final UserService userService;
    private final OrderRepository orderRepository;

    @GetMapping
    public String cart(Model model, HttpServletRequest httpServletRequest, Authentication authentication) {
        List<DishDto> dishes = fillCart(model, httpServletRequest, authentication);
        if (!model.containsAttribute("checkout")) {
            CheckoutDto checkout = new CheckoutDto();
            if (model.containsAttribute("user")) {
                User user = (User) model.getAttribute("user");
                checkout.setPhone(user.getPhone());
                orderRepository.findFirstByUser_IdAndAddressIsNotNullOrderByCreatedAtDesc(user.getId())
                        .ifPresent(order -> checkout.setAddress(order.getAddress()));
            }
            model.addAttribute("checkout", checkout);
        }
        return "cart/cart";
    }

    @PostMapping("decrease")
    public String decrease(@RequestParam Long dishId, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        httpServletResponse.addCookie(cartService.changeQuantityByDishId(httpServletRequest.getCookies(), dishId, true));
        return "redirect:/cart";
    }

    @PostMapping("increase")
    public String increase(@RequestParam Long dishId, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        httpServletResponse.addCookie(cartService.changeQuantityByDishId(httpServletRequest.getCookies(), dishId, false));
        return "redirect:/cart";
    }

    @PostMapping("remove")
    public String remove(@RequestParam Long dishId, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        httpServletResponse.addCookie(cartService.deleteDishFromCookieById(dishId, httpServletRequest.getCookies()));
        return "redirect:/cart";
    }

    private List<DishDto> fillCart(Model model, HttpServletRequest request, Authentication authentication) {
        List<DishDto> dishes = dishService.getDishesFromCookie(request.getCookies());
        if (dishes == null) {
            dishes = List.of();
        }
        model.addAttribute("dishes", dishes);
        model.addAttribute("totalPrice", orderService.getTotalPrice(dishes));
        model.addAttribute("cartCount", dishes.size());

        long restaurants = dishes.stream().map(DishDto::getRestaurantId).distinct().count();
        boolean mixedCart = restaurants > 1;
        model.addAttribute("mixedCart", mixedCart);
        if (!mixedCart && !dishes.isEmpty()) {
            model.addAttribute("cartRestaurant", dishes.get(0).getRestaurantName());
            model.addAttribute("cartRestaurantId", dishes.get(0).getRestaurantId());
        }
        if (isLoggedIn(authentication)) {
            model.addAttribute("user", userService.findByEmail(authentication.getName()));
        }
        return dishes;
    }

    private boolean isLoggedIn(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }
}
