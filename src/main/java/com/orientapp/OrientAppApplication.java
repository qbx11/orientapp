package com.orientapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punkt wejścia aplikacji OrientApp — platformy do zarządzania zawodami
 * w biegu na orientację.
 * <p>
 * Uruchamia kontekst Spring Boot wraz z autokonfiguracją (web, JPA, security,
 * Thymeleaf).
 */
@SpringBootApplication
public class OrientAppApplication {

    /**
     * Uruchamia aplikację Spring Boot.
     *
     * @param args argumenty wiersza poleceń
     */
    public static void main(String[] args) {
        SpringApplication.run(OrientAppApplication.class, args);
    }
}
