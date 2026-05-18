package com.energiaclara.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.energiaclara")
@EnableJpaRepositories(basePackages = "com.energiaclara")
@EntityScan(basePackages = "com.energiaclara")
public class EnergiaclaraApplication {
    public static void main(String[] args) {
        SpringApplication.run(EnergiaclaraApplication.class, args);
    }
}
