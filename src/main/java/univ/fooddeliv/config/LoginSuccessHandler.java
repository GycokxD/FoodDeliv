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
        boolean admin = hasAuthority(authentication, "ADMIN");
        boolean courier = hasAuthority(authentication, "COURIER");
        String next = safeNext(request.getParameter("next"));
        if (next != null && next.startsWith("/admin") && !admin) {
            next = null;
        }
        if (next != null && next.startsWith("/courier") && !courier) {
            next = null;
        }
        String target = next != null ? next : (admin ? "/admin" : courier ? "/courier" : "/");
        getRedirectStrategy().sendRedirect(request, response, target);
        clearAuthenticationAttributes(request);
    }

    private static boolean hasAuthority(Authentication authentication, String name) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> name.equals(authority.getAuthority()));
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
