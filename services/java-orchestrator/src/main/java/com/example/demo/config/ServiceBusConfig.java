package com.example.demo.config;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceBusConfig {

    @Bean
    public ServiceBusClientBuilder serviceBusClientBuilder(@Value("${azure.servicebus.connection-string}") String connectionString) {
        return new ServiceBusClientBuilder().connectionString(connectionString);
    }
}
