package org.example.userservice.config;

import org.example.userservice.model.User;
import org.example.userservice.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initUsers(UserRepository userRepository) {
        return args -> {
            if (userRepository.count() > 0) return;

            // IDs match the hardcoded user IDs in the BFF (demo=1, admin=2)
            userRepository.save(new User("demo",  "demo@ticketservice.com",  "Demo User"));
            userRepository.save(new User("admin", "admin@ticketservice.com", "Admin User"));
        };
    }
}