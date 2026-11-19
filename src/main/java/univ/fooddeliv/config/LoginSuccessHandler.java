package univ.fooddeliv.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ADMIN".equals(authority.getAuthority()));
        String next = safeNext(request.getParameter("next"));
        if (next != null && next.startsWith("/admin") && !admin) {
            next = null;
        }
        String target = next != null ? next : (admin ? "/admin" : "/");
        getRedirectStrategy().sendRedirect(request, response, target);
        clearAuthenticationAttributes(request);
    }

    public static String safeNext(String next) {
        if (next == null || next.isBlank()) {
            return null;
        }
        if (!next.startsWith("/") || next.startsWith("//") || next.contains("\\")
                || next.contains("://") || next.contains("\n") || next.contains("\r")
                || next.length() > 300) {
            return null;
        }
        return next;
    }
}
