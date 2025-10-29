package com.zainab.PearsonBank;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EnableRetry
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
                url = "https://github.com/sheismo/pearson-bank"
        )

)
public class PearsonBankApplication {

    public static void main(String[] args) {
        SpringApplication.run(PearsonBankApplication.class, args);
    }

}
