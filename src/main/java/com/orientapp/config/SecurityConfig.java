package com.orientapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;

/**
 * Konfiguracja bezpieczeństwa aplikacji (Spring Security).
 * <p>
 * Definiuje reguły autoryzacji żądań (dostęp do {@code /admin/**} tylko dla roli
 * ADMIN, pozostałe strony publiczne), logowanie formularzowe, wylogowanie oraz
 * koder haseł BCrypt. Zezwala na ramki dla konsoli H2 w trybie deweloperskim.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Koder haseł oparty na algorytmie BCrypt — używany przy tworzeniu
     * i weryfikacji haseł użytkowników.
     *
     * @return instancja {@link PasswordEncoder} (BCrypt)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Definiuje łańcuch filtrów bezpieczeństwa: reguły autoryzacji, logowanie
     * formularzowe, wylogowanie oraz nagłówki (m.in. dla konsoli H2).
     *
     * @param http obiekt konfiguracji bezpieczeństwa HTTP
     * @return zbudowany {@link SecurityFilterChain}
     * @throws Exception gdy budowa konfiguracji się nie powiedzie
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/", "/events/**", "/login", "/css/**", "/js/**", "/images/**", "/results/**", "/api/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/admin", true)
                .failureUrl("/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .permitAll()
            )
            .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
            .headers(headers -> headers
                .addHeaderWriter(new XFrameOptionsHeaderWriter(
                    XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN))
            );
        return http.build();
    }
}
