package univ.fooddeliv.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import univ.fooddeliv.dto.AuthDto;
import univ.fooddeliv.exceptions.UserAlreadyExistsException;
import univ.fooddeliv.config.LoginSuccessHandler;
import univ.fooddeliv.service.CartService;
import univ.fooddeliv.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final CartService cartService;

    @GetMapping("/login")
    public String showLoginForm(@RequestParam(required = false) String error,
                                @RequestParam(required = false) String disabled,
                                @RequestParam(required = false) String next,
                                Model model,
                                HttpServletRequest request) {
        model.addAttribute("next", LoginSuccessHandler.safeNext(next));
        if (disabled != null) {
            model.addAttribute("error", "Аккаунт отключён");
        } else if (error != null) {
            model.addAttribute("error", "Неверный email или пароль");
        }
        model.addAttribute("cartCount", cartService.countDishesInCart(request.getCookies()));
        return "auth/login";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("authDto", new AuthDto());
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid AuthDto authDto,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        if (!authDto.getPassword().equals(authDto.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.authDto", "Пароли не совпадают");
            return "auth/register";
        }

        try {
            userService.registerUser(authDto);
            redirectAttributes.addFlashAttribute("message", "Регистрация прошла успешно. Теперь можно войти.");
            redirectAttributes.addFlashAttribute("messageType", "success");
            return "redirect:/auth/login";
        } catch (UserAlreadyExistsException e) {
            bindingResult.rejectValue("email", "error.authDto", e.getMessage());
            return "auth/register";
        }
    }
}
