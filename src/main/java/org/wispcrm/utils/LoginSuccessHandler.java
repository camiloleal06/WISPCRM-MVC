package org.wispcrm.utils;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    public LoginSuccessHandler() {
        setDefaultTargetUrl("/index.html");
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        boolean isCajero = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CAJERO"));
        if (isCajero) {
            response.sendRedirect("/index.html#/facturas");
        } else {
            super.onAuthenticationSuccess(request, response, authentication);
        }
    }
}
