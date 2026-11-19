package univ.fooddeliv.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import univ.fooddeliv.service.RestaurantService;

@Controller
@RequiredArgsConstructor
public class MainController {
    private final RestaurantService restaurantService;

    @GetMapping
    public String showMainPage(Model model) {
        model.addAttribute("featured", restaurantService.getAllRestaurants(0, 6).getContent());
        return "home";
    }
}
