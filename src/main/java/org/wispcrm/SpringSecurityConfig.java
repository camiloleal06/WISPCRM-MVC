package org.wispcrm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.wispcrm.modelo.AppUser;
import org.wispcrm.utils.LoginSuccessHandler;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Configuration
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private LoginSuccessHandler successHandler;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .cors().and()
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("/login.html", "/login", "/css/**", "/js/**",
                        "/plugins/**", "/dist/**", "/manifest.json", "/sw.js",
                        "/descargarfactura/**", "/descargarorden/**", "/descargarpago/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginPage("/login.html")
                .loginProcessingUrl("/login")
                .successHandler(successHandler)
                .failureUrl("/login.html?error=true")
                .permitAll()
                .and()
                .logout()
                .logoutSuccessUrl("/login.html?logout=true")
                .permitAll()
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(authEntryPoint());
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
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
                response.sendRedirect("/login.html");
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
