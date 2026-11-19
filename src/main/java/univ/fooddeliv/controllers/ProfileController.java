package univ.fooddeliv.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import univ.fooddeliv.dto.ProfileUpdateDto;
import univ.fooddeliv.models.Order;
import univ.fooddeliv.models.OrderStatus;
import univ.fooddeliv.models.User;
import univ.fooddeliv.repository.UserRepository;
import univ.fooddeliv.service.CartService;
import univ.fooddeliv.service.UserService;
import univ.fooddeliv.service.impl.OrderTrackingService;
import univ.fooddeliv.utils.DateFormatUtil;
import univ.fooddeliv.utils.FileUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/profile")
public class ProfileController {
    private final UserRepository userRepository;
    private final FileUtil fileUtil;
    private final UserService userService;
    private final CartService cartService;
    private final OrderTrackingService orderTrackingService;

    @GetMapping
    public String showProfilePage(@AuthenticationPrincipal UserDetails userDetails, Model model, HttpServletRequest request) {
        User user = userService.findByEmail(userDetails.getUsername());
        List<Order> orders = userService.getUserOrders(userDetails.getUsername());
        orderTrackingService.refresh(orders);

        List<Map<String, Object>> orderViews = orders.stream().map(order -> {
            Map<String, Object> view = new HashMap<>();
            OrderStatus status = order.getStatus() == null ? OrderStatus.NEW : order.getStatus();
            view.put("id", order.getId());
            view.put("totalPrice", order.getTotalPrice());
            view.put("createdAtFormatted", DateFormatUtil.format(order.getCreatedAt()));
            view.put("statusLabel", status.getLabel());
            view.put("statusBadge", status.getBadge());
            view.put("paymentLabel", order.getPaymentMethod() == null ? null : order.getPaymentMethod().getLabel());
            view.put("paymentState", order.getPaymentStatus() == null ? null : order.getPaymentStatus().getLabel());
            return view;
        }).collect(Collectors.toList());

        model.addAttribute("user", user);
        model.addAttribute("createdAtFormatted", DateFormatUtil.format(user.getCreatedAt()));
        model.addAttribute("orders", orderViews);
        model.addAttribute("cartCount", cartService.countDishesInCart(request.getCookies()));

        return "profile/profile";
    }

    @PostMapping
    public String updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                @Valid @ModelAttribute("profileForm") ProfileUpdateDto profileUpdateDto,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("profileError", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/profile";
        }
        try {
            userService.updateProfile(userDetails.getUsername(), profileUpdateDto);
            redirectAttributes.addFlashAttribute("message", "Профиль обновлён");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("profileError", e.getMessage());
        }
        return "redirect:/profile";
    }

    @PostMapping("/avatar")
    public String uploadAvatar(@AuthenticationPrincipal UserDetails userDetails,
                               @RequestParam("file") MultipartFile file,
                               RedirectAttributes redirectAttributes) {
        User user = userService.findByEmail(userDetails.getUsername());
        try {
            String newFileName = fileUtil.saveAvatar(file);
            fileUtil.deleteAvatar(user.getAvatar());
            user.setAvatar(newFileName);
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("message", "Аватар обновлён");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (IOException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("profileError", e.getMessage());
        }

        return "redirect:/profile";
    }
}
