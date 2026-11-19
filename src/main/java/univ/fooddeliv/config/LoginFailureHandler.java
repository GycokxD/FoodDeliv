package univ.fooddeliv.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    public LoginFailureHandler() {
        setDefaultFailureUrl("/auth/login?error");
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        if (isDisabled(exception)) {
            getRedirectStrategy().sendRedirect(request, response, "/auth/login?disabled");
            return;
        }
        super.onAuthenticationFailure(request, response, exception);
    }

    private boolean isDisabled(Throwable exception) {
        Throwable current = exception;
        while (current != null) {
            if (current instanceof DisabledException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
