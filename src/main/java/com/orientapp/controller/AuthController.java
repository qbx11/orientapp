package com.orientapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Kontroler uwierzytelniania.
 * <p>
 * Udostępnia stronę logowania. Samą obsługę logowania, wylogowania i sesji
 * realizuje Spring Security (konfiguracja w {@link com.orientapp.config.SecurityConfig}).
 */
@Controller
public class AuthController {

    /**
     * Wyświetla formularz logowania.
     *
     * @return nazwa widoku strony logowania
     */
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
}
