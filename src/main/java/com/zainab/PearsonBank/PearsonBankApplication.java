package com.zainab.PearsonBank;

import com.zainab.PearsonBank.entity.User;
import com.zainab.PearsonBank.repository.UserRepository;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableAsync
@EnableRetry
@EnableScheduling
@OpenAPIDefinition(
        info = @Info(
                title ="Pearson Fintech App",
                description ="Swagger API Documentation for Pearson Fintech Application",
                version ="v1.0",
                contact = @Contact(
                        name="Zainab Ajumobi",
                        email="ajumobizainab@gmail.com",
                        url = "https://github.com/sheismo"
                ),
                license = @License(name = "Pearson Fintech App")
        ),
        externalDocs = @ExternalDocumentation(
                description = "Pearson Fintech App External Documentation",
                url = "https://github.com/sheismo/pearson-bank-backend"
        )

)
public class PearsonBankApplication {
    public static void main(String[] args) {
        SpringApplication.run(PearsonBankApplication.class, args);
    }


    @Bean
    CommandLineRunner seedDatabase(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {

            if (userRepository.existsByEmail("admin@pearsonbank.com")) {
                return;
            }

            User admin = new User();
            admin.setEmail("admin@pearsonbank.com");
            admin.setAppPassword(passwordEncoder.encode("Admin1234567@"));
            admin.setRole(User.Role.ADMIN);
            admin.setProfileEnabled(true);
            userRepository.save(admin);

            System.out.println("✅ Admin seeded successfully");
        };
    }
}
