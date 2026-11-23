package univ.fooddeliv.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import univ.fooddeliv.models.Dish;
import univ.fooddeliv.models.OrderStatus;
import univ.fooddeliv.models.Restaurant;
import univ.fooddeliv.service.impl.AdminService;
import univ.fooddeliv.service.impl.AdminStatsService;
import univ.fooddeliv.service.impl.AppSettingsService;

import java.util.Map;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private static final ObjectMapper JSON = new ObjectMapper();

    private final AdminStatsService adminStatsService;
    private final AdminService adminService;
    private final AppSettingsService appSettingsService;

    @GetMapping
    public String dashboard(Model model) throws JsonProcessingException {
        Map<String, Object> dashboard = adminStatsService.dashboard();
        model.addAllAttributes(dashboard);
        model.addAttribute("statusLabelsJson", JSON.writeValueAsString(dashboard.get("statusLabels")));
        model.addAttribute("statusCountsJson", JSON.writeValueAsString(dashboard.get("statusCounts")));
        model.addAttribute("restaurantLabelsJson", JSON.writeValueAsString(dashboard.get("restaurantLabels")));
        model.addAttribute("restaurantRevenueJson", JSON.writeValueAsString(dashboard.get("restaurantRevenue")));
        model.addAttribute("dayLabelsJson", JSON.writeValueAsString(dashboard.get("dayLabels")));
        model.addAttribute("dayOrdersJson", JSON.writeValueAsString(dashboard.get("dayOrders")));
        model.addAttribute("dayRevenueJson", JSON.writeValueAsString(dashboard.get("dayRevenue")));
        return "admin/dashboard";
    }

    @GetMapping("/orders")
    public String orders(Model model) {
        model.addAttribute("orders", adminService.orders());
        model.addAttribute("statuses", OrderStatus.values());
        return "admin/orders";
    }

    @PostMapping("/orders/{id}/status")
    public String updateOrderStatus(@PathVariable Long id,
                                    @RequestParam OrderStatus status,
                                    RedirectAttributes redirectAttributes) {
        return update(redirectAttributes, "/admin/orders", () -> adminService.updateOrderStatus(id, status), "Статус заказа обновлён");
    }

    @GetMapping("/restaurants")
    public String restaurants(Model model) {
        model.addAttribute("restaurants", adminService.restaurants());
        return "admin/restaurants";
    }

    @GetMapping("/restaurants/new")
    public String newRestaurant(Model model) {
        model.addAttribute("restaurant", new Restaurant());
        return "admin/restaurant-form";
    }

    @GetMapping("/restaurants/{id}/edit")
    public String editRestaurant(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("restaurant", adminService.restaurant(id));
        } catch (RuntimeException e) {
            return fail(redirectAttributes, "/admin/restaurants", e);
        }
        return "admin/restaurant-form";
    }

    @PostMapping("/restaurants")
    public String createRestaurant(@RequestParam String name,
                                   @RequestParam String address,
                                   @RequestParam(required = false) String image,
                                   @RequestParam(required = false) MultipartFile file,
                                   RedirectAttributes redirectAttributes) {
        return update(redirectAttributes, "/admin/restaurants", () -> adminService.saveRestaurant(null, name, address, image, file), "Ресторан добавлен");
    }

    @PostMapping("/restaurants/{id}")
    public String updateRestaurant(@PathVariable Long id,
                                   @RequestParam String name,
                                   @RequestParam String address,
                                   @RequestParam(required = false) String image,
                                   @RequestParam(required = false) MultipartFile file,
                                   RedirectAttributes redirectAttributes) {
        return update(redirectAttributes, "/admin/restaurants", () -> adminService.saveRestaurant(id, name, address, image, file), "Ресторан сохранён");
    }

    @PostMapping("/restaurants/{id}/delete")
    public String deleteRestaurant(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        return update(redirectAttributes, "/admin/restaurants", () -> adminService.deleteRestaurant(id), "Ресторан удалён");
    }

    @GetMapping("/dishes")
    public String dishes(Model model) {
        model.addAttribute("dishes", adminService.dishes());
        model.addAttribute("restaurants", adminService.restaurants());
        return "admin/dishes";
    }

    @GetMapping("/dishes/new")
    public String newDish(Model model) {
        model.addAttribute("dish", new Dish());
        model.addAttribute("restaurants", adminService.restaurants());
        return "admin/dish-form";
    }

    @GetMapping("/dishes/{id}/edit")
    public String editDish(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("dish", adminService.dish(id));
            model.addAttribute("restaurants", adminService.restaurants());
        } catch (RuntimeException e) {
            return fail(redirectAttributes, "/admin/dishes", e);
        }
        return "admin/dish-form";
    }

    @PostMapping("/dishes")
    public String createDish(@RequestParam Long restaurantId,
                             @RequestParam String name,
                             @RequestParam(required = false) String description,
                             @RequestParam(required = false) Double price,
                             @RequestParam(required = false) String image,
                             @RequestParam(required = false) MultipartFile file,
                             RedirectAttributes redirectAttributes) {
        return update(redirectAttributes, "/admin/dishes",
                () -> adminService.saveDish(null, restaurantId, name, description, price, image, file),
                "Блюдо добавлено");
    }

    @PostMapping("/dishes/{id}")
    public String updateDish(@PathVariable Long id,
                             @RequestParam Long restaurantId,
                             @RequestParam String name,
                             @RequestParam(required = false) String description,
                             @RequestParam(required = false) Double price,
                             @RequestParam(required = false) String image,
                             @RequestParam(required = false) MultipartFile file,
                             RedirectAttributes redirectAttributes) {
        return update(redirectAttributes, "/admin/dishes",
                () -> adminService.saveDish(id, restaurantId, name, description, price, image, file),
                "Блюдо сохранено");
    }

    @PostMapping("/dishes/{id}/delete")
    public String deleteDish(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        return update(redirectAttributes, "/admin/dishes", () -> adminService.deleteDish(id), "Блюдо удалено");
    }

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", adminService.users());
        model.addAttribute("roles", adminService.roles());
        return "admin/users";
    }

    @PostMapping("/users/{id}/role")
    public String updateRole(@PathVariable Long id,
                             @RequestParam String role,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        return update(redirectAttributes, "/admin/users",
                () -> adminService.updateUserRole(id, role, userDetails.getUsername()),
                "Роль обновлена");
    }

    @PostMapping("/users/{id}/enabled")
    public String updateEnabled(@PathVariable Long id,
                                @RequestParam boolean enabled,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        return update(redirectAttributes, "/admin/users",
                () -> adminService.updateUserEnabled(id, enabled, userDetails.getUsername()),
                enabled ? "Пользователь включён" : "Пользователь отключён");
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        return update(redirectAttributes, "/admin/users",
                () -> adminService.deleteUser(id, userDetails.getUsername()),
                "Пользователь удалён");
    }

    @GetMapping("/settings")
    public String settings(Model model) {
        model.addAttribute("settings", appSettingsService.get());
        return "admin/settings";
    }

    @PostMapping("/settings")
    public String updateSettings(@RequestParam int acceptedSeconds,
                                 @RequestParam int cookingSeconds,
                                 @RequestParam int deliverySeconds,
                                 @RequestParam(required = false) String supportPhone,
                                 @RequestParam(required = false) String city,
                                 @RequestParam(defaultValue = "false") boolean demoCouriers,
                                 RedirectAttributes redirectAttributes) {
        return update(redirectAttributes, "/admin/settings",
                () -> appSettingsService.update(acceptedSeconds, cookingSeconds, deliverySeconds, supportPhone, city, demoCouriers),
                "Настройки сохранены");
    }

    private String update(RedirectAttributes redirectAttributes, String path, Runnable action, String success) {
        try {
            action.run();
            redirectAttributes.addFlashAttribute("message", success);
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (RuntimeException e) {
            return fail(redirectAttributes, path, e);
        }
        return "redirect:" + path;
    }

    private String fail(RedirectAttributes redirectAttributes, String path, RuntimeException e) {
        redirectAttributes.addFlashAttribute("message", e.getMessage() == null ? "Не удалось выполнить действие" : e.getMessage());
        redirectAttributes.addFlashAttribute("messageType", "danger");
        return "redirect:" + path;
    }
}
