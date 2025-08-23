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
import org.wispcrm.utils.LoginSuccessHandler;

@Configuration
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter {
    private static final String USER = "USER";
    private static final String ADMIN = "ADMIN";
    @Autowired
    private LoginSuccessHandler successHandler;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/build/", "/dist/**", "/plugins/**", "/docs/**")
                .permitAll().antMatchers("/descargarfactura/**").permitAll()
                .antMatchers("/descargarorden/**").permitAll()
                .antMatchers("/api/**").permitAll()
                .antMatchers("/descargarpago/**").permitAll()
                .antMatchers("/cliente/**").hasAnyRole(USER)
                .antMatchers("/editar/**").hasAnyRole(ADMIN)
                .antMatchers("/eliminar/**").hasAnyRole(ADMIN)
                .antMatchers("/listar/**").hasAnyRole(USER, ADMIN)
                .antMatchers("/layout/**").hasAnyRole(USER)
                .antMatchers("/plan/**").hasAnyRole(ADMIN, USER)
                .antMatchers("/factura/**").hasAnyRole(ADMIN)
                .antMatchers("/vercliente").hasAnyRole(ADMIN).anyRequest()
                .authenticated().and().formLogin()
                .successHandler(successHandler).permitAll().and().logout()
                .permitAll().and().exceptionHandling()
                .accessDeniedPage("/error_403");

    }

    @Autowired
    public void configurerGlobal(AuthenticationManagerBuilder build)
            throws Exception {
        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        UserBuilder users = User.builder().passwordEncoder(encoder::encode);
        build.inMemoryAuthentication().withUser(
                        users.username("camiloleal")
                                .password("T3CN02020+-+")
                                .roles(ADMIN)).withUser(
                        users.username("fernando")
                                .password("Fernando2020+-")
                                .roles(USER)).withUser(
                        users.username("audiluz")
                                .password("audiluz").roles(USER))
                .withUser(users.username("jose")
                        .password("jose")
                        .roles(USER));
    }
}
