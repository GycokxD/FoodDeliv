package univ.fooddeliv.exceptions;

import univ.fooddeliv.dto.AuthDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    @ExceptionHandler(UserAlreadyExistsException.class)
    public String handleUserAlreadyExists(UserAlreadyExistsException e,
                                          Model model,
                                          @ModelAttribute("authDTO") AuthDto authDto) {
        model.addAttribute("error", e.getMessage());
        return "register";
    }

    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, RedirectAttributes redirectAttributes) throws Exception {
        if (e instanceof NoHandlerFoundException || e instanceof NoResourceFoundException) {
            throw e;
        }
        log.error("Request failed", e);
        redirectAttributes.addFlashAttribute("message", e.getMessage());
        redirectAttributes.addFlashAttribute("messageType", "danger");
        return "redirect:/restaurants";
    }
}
