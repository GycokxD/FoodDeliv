package univ.fooddeliv.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import univ.fooddeliv.config.MapsProperties;
import univ.fooddeliv.dto.CourierOrderView;
import univ.fooddeliv.dto.RouteDto;
import univ.fooddeliv.models.Order;
import univ.fooddeliv.models.Restaurant;
import univ.fooddeliv.service.impl.CourierService;
import univ.fooddeliv.service.impl.RoutingService;
import univ.fooddeliv.utils.DateFormatUtil;
import univ.fooddeliv.utils.GeoUtil;
import univ.fooddeliv.utils.OrderUtil;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

@Controller
@RequiredArgsConstructor
public class CourierController {
    private static final ObjectMapper JSON = new ObjectMapper();

    private final CourierService courierService;
    private final RoutingService routingService;
    private final MapsProperties mapsProperties;

    @GetMapping("/courier")
    public String index(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("activeOrders", courierService.activeOrders(userDetails.getUsername()).stream().map(this::view).toList());
        model.addAttribute("availableOrders", courierService.availableOrders().stream().map(this::view).toList());
        return "courier/index";
    }

    @GetMapping("/courier/orders/{id}")
    public String order(@PathVariable Long id,
                        @AuthenticationPrincipal UserDetails userDetails,
                        Model model,
                        RedirectAttributes redirectAttributes) throws JsonProcessingException {
        Order order;
        try {
            order = courierService.orderForCourier(id, userDetails.getUsername());
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("message", "Этот заказ не назначен на вас");
            redirectAttributes.addFlashAttribute("messageType", "warning");
            return "redirect:/courier";
        }
        model.addAttribute("order", order);
        model.addAttribute("view", view(order));
        model.addAttribute("geoJson", JSON.writeValueAsString(geo(order)));
        return "courier/order";
    }

    @PostMapping("/courier/orders/{id}/take")
    public String take(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails, RedirectAttributes redirectAttributes) {
        return act(redirectAttributes, "/courier", "/courier/orders/" + id,
                () -> courierService.take(id, userDetails.getUsername()),
                "Заказ ваш. Включите геолокацию и езжайте в ресторан");
    }

    @PostMapping("/courier/orders/{id}/pickup")
    public String pickUp(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails, RedirectAttributes redirectAttributes) {
        return act(redirectAttributes, "/courier/orders/" + id, "/courier/orders/" + id,
                () -> courierService.pickUp(id, userDetails.getUsername()),
                "Заказ забран. Клиент видит вас на карте");
    }

    @PostMapping("/courier/orders/{id}/deliver")
    public String deliver(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails, RedirectAttributes redirectAttributes) {
        return act(redirectAttributes, "/courier/orders/" + id, "/courier",
                () -> courierService.deliver(id, userDetails.getUsername()),
                "Заказ #" + id + " доставлен");
    }

    @PostMapping("/api/courier/orders/{id}/location")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> location(@PathVariable Long id,
                                                        @RequestBody Map<String, Double> body,
                                                        @AuthenticationPrincipal UserDetails userDetails) {
        Double lat = body.get("lat");
        Double lng = body.get("lng");
        if (!GeoUtil.isValid(lat, lng)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Некорректные координаты"));
        }
        try {
            boolean saved = courierService.updateLocation(id, userDetails.getUsername(), lat, lng);
            return ResponseEntity.ok(Map.of("saved", saved));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(404).body(Map.of("error", "Заказ не найден"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(Map.of("error", e.getMessage()));
        }
    }

    private String act(RedirectAttributes redirectAttributes, String failPath, String successPath,
                       Supplier<Order> action, String success) {
        try {
            action.get();
            redirectAttributes.addFlashAttribute("message", success);
            redirectAttributes.addFlashAttribute("messageType", "success");
            return "redirect:" + successPath;
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "warning");
            return "redirect:" + failPath;
        }
    }

    private CourierOrderView view(Order order) {
        Optional<Restaurant> restaurant = OrderUtil.restaurantOf(order);
        Double distanceKm = OrderUtil.restaurantPoint(order)
                .flatMap(from -> OrderUtil.destinationPoint(order)
                        .map(to -> Math.round(GeoUtil.distanceMeters(from[0], from[1], to[0], to[1]) / 100.0) / 10.0))
                .orElse(null);
        return new CourierOrderView(
                order.getId(),
                order.getStatus().getLabel(),
                order.getStatus().getBadge(),
                restaurant.map(Restaurant::getName).orElse("Ресторан"),
                restaurant.map(Restaurant::getAddress).orElse(""),
                order.getAddress(),
                order.getNote(),
                order.getTotalPrice() == null ? 0 : order.getTotalPrice(),
                distanceKm,
                DateFormatUtil.format(order.getCreatedAt()),
                order.getPaymentMethod() == null ? "Не указана" : order.getPaymentMethod().getLabel(),
                Boolean.TRUE.equals(order.getPaid())
        );
    }

    private Map<String, Object> geo(Order order) {
        Map<String, Object> geo = new LinkedHashMap<>();
        geo.put("center", List.of(mapsProperties.centerLat(), mapsProperties.centerLng()));
        Optional<double[]> restaurant = OrderUtil.restaurantPoint(order);
        Optional<double[]> destination = OrderUtil.destinationPoint(order);
        restaurant.ifPresent(point -> geo.put("restaurant", point));
        destination.ifPresent(point -> geo.put("destination", point));
        OrderUtil.courierPoint(order).ifPresent(point -> geo.put("courier", point));
        if (restaurant.isPresent() && destination.isPresent()) {
            RouteDto route = routingService.route(restaurant.get()[0], restaurant.get()[1], destination.get()[0], destination.get()[1]);
            geo.put("route", route.points());
            geo.put("routeKm", Math.round(route.distanceMeters() / 100.0) / 10.0);
            geo.put("routeMinutes", Math.max(1, Math.round(route.durationSeconds() / 60)));
        }
        return geo;
    }
}
