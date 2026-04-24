package com.orientapp.config;

import com.orientapp.model.Role;
import com.orientapp.repository.AppUserRepository;
import com.orientapp.service.AppUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final AppUserRepository userRepository;
    private final AppUserService userService;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            userService.createUser(
                    "admin@123",
                    "admin123",
                    "Administrator",
                    null,
                    Role.ADMIN
            );
            
        }
    }
}
