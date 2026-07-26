package org.wispcrm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.wispcrm.modelo.AppUser;
import org.wispcrm.utils.LoginSuccessHandler;

import javax.servlet.http.HttpServletResponse;

@Configuration
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private LoginSuccessHandler successHandler;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("/dist/**", "/plugins/**", "/css/**", "/js/**", "/manifest.json", "/sw.js").permitAll()
                .antMatchers("/descargarfactura/**", "/descargarorden/**", "/descargarpago/**").permitAll()
                .antMatchers("/login").permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler(successHandler)
                .failureUrl("/login?error=true")
                .permitAll()
                .and()
                .logout().logoutSuccessUrl("/login?logout=true").permitAll()
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(authEntryPoint());
    }

    private AuthenticationEntryPoint authEntryPoint() {
        return (request, response, authException) -> {
            String uri = request.getRequestURI();
            String accept = request.getHeader("Accept");
            boolean isApi = uri.startsWith("/api/");
            boolean isAjax = accept != null && accept.contains("application/json");

            if (isApi || isAjax) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"status\":\"error\",\"message\":\"Sesion expirada\"}");
            } else {
                response.sendRedirect("/login");
            }
        };
    }

    @Autowired
    public void configurerGlobal(AuthenticationManagerBuilder build) throws Exception {
        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        UserBuilder users = User.builder().passwordEncoder(encoder::encode);

        for (AppUser appUser : AppUser.values()) {
            build.inMemoryAuthentication().withUser(
                    users.username(appUser.getUsername())
                            .password(appUser.getPassword())
                            .roles(appUser.getRole().name())
            );
        }
    }
}
