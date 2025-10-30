package ru.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .requiresChannel(channel ->
                        channel.anyRequest().requiresSecure())
                .authorizeHttpRequests((requests) ->
                        requests.requestMatchers("/profile", "/stats/**").authenticated()
                                .requestMatchers(HttpMethod.PATCH, "/api/urls/**").authenticated()
                                .requestMatchers(HttpMethod.DELETE, "/api/urls/**").authenticated()
                                .anyRequest().permitAll())
                .formLogin(form -> form.loginPage("/login").defaultSuccessUrl("/main").permitAll())
                .logout(logout -> logout.logoutUrl("/logout").logoutSuccessUrl("/main")
                        .invalidateHttpSession(true).deleteCookies("JSESSIONID").permitAll())
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
