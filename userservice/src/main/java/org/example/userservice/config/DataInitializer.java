package org.example.userservice.config;

import org.example.userservice.model.User;
import org.example.userservice.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initUsers(UserService userService,
                                       org.example.userservice.repository.UserRepository userRepository) {
        return args -> {
            if (userRepository.count() > 0) return;

            // Passwords will be BCrypt-hashed by UserService.createUser
            userService.createUser(new User("demo",  "demo@ticketservice.com",  "Demo User",  "demo",  "USER"));
            userService.createUser(new User("admin", "admin@ticketservice.com", "Admin User", "admin", "ADMIN"));
        };
    }
}
