package univ.fooddeliv.controllers;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import univ.fooddeliv.config.MapsProperties;
import univ.fooddeliv.dto.CheckoutDto;
import univ.fooddeliv.exceptions.PaymentDeclinedException;
import univ.fooddeliv.dto.DishDto;
import univ.fooddeliv.dto.OrderTrackingDto;
import univ.fooddeliv.models.Order;
import univ.fooddeliv.models.User;
import univ.fooddeliv.service.CartService;
import univ.fooddeliv.service.DishService;
import univ.fooddeliv.service.OrderService;
import univ.fooddeliv.service.UserService;
import univ.fooddeliv.service.impl.OrderTrackingService;
import univ.fooddeliv.utils.DateFormatUtil;
import univ.fooddeliv.utils.GeoUtil;
import univ.fooddeliv.utils.PaymentDetails;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("orders")
public class OrderController {
    private final OrderService orderService;
    private final DishService dishService;
    private final UserService userService;
    private final CartService cartService;
    private final OrderTrackingService orderTrackingService;
    private final MapsProperties mapsProperties;

    @PostMapping("make-order")
    public String makeOrder(@Valid @ModelAttribute("checkout") CheckoutDto checkout,
                            BindingResult bindingResult,
                            HttpServletResponse response,
                            HttpServletRequest request,
                            Authentication auth,
                            RedirectAttributes redirectAttributes) {
        List<DishDto> dishes = dishService.getDishesFromCookie(request.getCookies());
        if (dishes == null || dishes.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "Корзина пуста");
            redirectAttributes.addFlashAttribute("messageType", "warning");
            return "redirect:/cart";
        }
        long restaurants = dishes.stream().map(DishDto::getRestaurantId).distinct().count();
        if (restaurants > 1) {
            redirectAttributes.addFlashAttribute("message", "В одном заказе могут быть блюда только из одного ресторана");
            redirectAttributes.addFlashAttribute("messageType", "warning");
            return "redirect:/cart";
        }
        PaymentDetails.validate(checkout, bindingResult);
        validateDeliveryPoint(checkout, bindingResult);
        if (bindingResult.hasErrors()) {
            checkout.setCardCvv(null);
            String errors = bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(". "));
            redirectAttributes.addFlashAttribute("checkout", checkout);
            redirectAttributes.addFlashAttribute("checkoutError", errors);
            return "redirect:/cart";
        }
        Order created;
        try {
            created = orderService.makeOrder(dishes, auth, checkout);
        } catch (PaymentDeclinedException e) {
            checkout.setCardCvv(null);
            redirectAttributes.addFlashAttribute("checkout", checkout);
            redirectAttributes.addFlashAttribute("checkoutError", e.getMessage() + ". Деньги не списаны, заказ не создан");
            return "redirect:/cart";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "warning");
            return "redirect:/cart";
        }
        Cookie cookie = new Cookie("cart", "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
        redirectAttributes.addFlashAttribute("message", "Заказ принят. На этой странице видно готовку и курьера");
        redirectAttributes.addFlashAttribute("messageType", "success");
        return "redirect:/orders/" + created.getId();
    }

    private void validateDeliveryPoint(CheckoutDto checkout, BindingResult bindingResult) {
        if (!GeoUtil.isValid(checkout.getLatitude(), checkout.getLongitude())) {
            bindingResult.reject("location", "Отметьте адрес доставки на карте");
            return;
        }
        double km = GeoUtil.distanceMeters(mapsProperties.centerLat(), mapsProperties.centerLng(),
                checkout.getLatitude(), checkout.getLongitude()) / 1000;
        if (km > mapsProperties.deliveryRadiusKm()) {
            bindingResult.reject("location", String.format("Адрес вне зоны доставки: доставляем в пределах %.0f км от центра города",
                    mapsProperties.deliveryRadiusKm()));
        }
    }

    @GetMapping("/{id}/tracking")
    @ResponseBody
    public OrderTrackingDto tracking(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        return orderTrackingService.trackForUser(id, userDetails.getUsername());
    }

    @GetMapping("/{id}")
    public String getOrderDetails(@PathVariable Long id,
                                  @AuthenticationPrincipal UserDetails userDetails,
                                  Model model) {
        Order order = orderService.getOrderByIdForUser(id, userDetails.getUsername());
        User user = userService.findByEmail(userDetails.getUsername());
        OrderTrackingDto tracking = orderTrackingService.trackForUser(id, userDetails.getUsername());
        model.addAttribute("order", order);
        model.addAttribute("tracking", tracking);
        model.addAttribute("createdAtFormatted", DateFormatUtil.format(order.getCreatedAt()));
        model.addAttribute("user", user);
        model.addAttribute("canCancel", tracking.isCanCancel());
        return "order/details";
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails userDetails,
                         RedirectAttributes redirectAttributes) {
        boolean cancelled = orderService.cancelIfNew(id, userDetails.getUsername());
        if (cancelled) {
            redirectAttributes.addFlashAttribute("message", "Заказ отменён. Если оплата уже прошла, деньги вернутся на карту");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } else {
            redirectAttributes.addFlashAttribute("message", "Отменить можно только новый заказ");
            redirectAttributes.addFlashAttribute("messageType", "warning");
        }
        return "redirect:/orders/" + id;
    }

    @PostMapping("/{id}/repeat")
    public String repeat(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails userDetails,
                         HttpServletResponse response,
                         RedirectAttributes redirectAttributes) {
        try {
            response.addCookie(cartService.setCart(orderService.toCartCookie(id, userDetails.getUsername())));
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "warning");
            return "redirect:/orders/" + id;
        }
        redirectAttributes.addFlashAttribute("message", "Корзина заполнена блюдами из заказа");
        redirectAttributes.addFlashAttribute("messageType", "success");
        return "redirect:/cart";
    }
}
