package com.example.demo;

import com.example.demo.model.Product;
import com.example.demo.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class DataInitializer {

    @Bean
    CommandLineRunner initializeData(ProductRepository productRepository) {
        return args -> {
            log.info("Initializing sample products...");

            Product product1 = Product.builder()
                    .name("Wireless Headphones")
                    .price(79.99)
                    .description("High-quality Bluetooth wireless headphones with noise cancellation")
                    .build();

            Product product2 = Product.builder()
                    .name("USB-C Cable")
                    .price(12.99)
                    .description("Durable 2-meter USB-C to USB-C charging and data cable")
                    .build();

            Product product3 = Product.builder()
                    .name("Laptop Stand")
                    .price(39.99)
                    .description("Aluminum adjustable laptop stand for better ergonomics")
                    .build();

            productRepository.save(product1);
            productRepository.save(product2);
            productRepository.save(product3);

            log.info("Sample products initialized successfully");
        };
    }
}
