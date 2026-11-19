package univ.fooddeliv.controllers;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class AppErrorController implements ErrorController {

    @RequestMapping("/error")
    public String error(HttpServletRequest request, HttpServletResponse response) {
        Object code = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int status = 500;
        if (code != null) {
            try {
                status = Integer.parseInt(code.toString());
            } catch (NumberFormatException ignored) {
                status = 500;
            }
        }
        response.setStatus(status);
        if (status == 404) {
            return "error/404";
        }
        if (status == 403) {
            return "error/403";
        }
        return "error/error";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "error/403";
    }
}
