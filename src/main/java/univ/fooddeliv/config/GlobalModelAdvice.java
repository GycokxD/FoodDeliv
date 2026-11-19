package univ.fooddeliv.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import univ.fooddeliv.models.Order;
import univ.fooddeliv.models.OrderStatus;
import univ.fooddeliv.models.User;
import univ.fooddeliv.repository.OrderRepository;
import univ.fooddeliv.repository.UserRepository;
import univ.fooddeliv.service.CartService;
import univ.fooddeliv.service.impl.AppSettingsService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAdvice {

    private final CartService cartService;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final AppSettingsService appSettingsService;

    @ModelAttribute
    public void addGlobalAttributes(HttpServletRequest request) {
        CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (token != null) {
            request.setAttribute("_csrf", token);
        }
    }

    @ModelAttribute("cartCount")
    public int cartCount(HttpServletRequest request) {
        return cartService.countDishesInCart(request.getCookies());
    }

    @ModelAttribute("currentUser")
    public User currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return userRepository.findByEmail(authentication.getName()).orElse(null);
    }

    @ModelAttribute("isAdmin")
    public boolean isAdmin() {
        User user = currentUser();
        return user != null && user.getRole() != null && "ADMIN".equals(user.getRole().getRole());
    }

    @ModelAttribute("isCourier")
    public boolean isCourier() {
        User user = currentUser();
        return user != null && user.getRole() != null && "COURIER".equals(user.getRole().getRole());
    }

    @ModelAttribute("appSettings")
    public Object appSettings() {
        return appSettingsService.get();
    }

    @ModelAttribute("loginNext")
    public String loginNext(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri.startsWith("/auth") || "/error".equals(uri) || "/access-denied".equals(uri)) {
            return "";
        }
        String query = request.getQueryString();
        return query == null ? uri : uri + "?" + query;
    }

    @ModelAttribute("activeOrder")
    public Map<String, Object> activeOrder() {
        User user = currentUser();
        if (user == null) {
            return null;
        }
        return orderRepository.findFirstByUser_IdAndStatusInOrderByCreatedAtDesc(
                user.getId(),
                List.of(OrderStatus.NEW, OrderStatus.COOKING, OrderStatus.ON_THE_WAY)
        ).map(this::activeOrderView).orElse(null);
    }

    private Map<String, Object> activeOrderView(Order order) {
        Map<String, Object> view = new HashMap<>();
        view.put("id", order.getId());
        view.put("label", order.getStatus() == null ? "Новый" : order.getStatus().getLabel());
        return view;
    }
}
