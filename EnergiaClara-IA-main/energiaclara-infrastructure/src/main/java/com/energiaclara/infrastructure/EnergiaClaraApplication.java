package com.energiaclara.infrastructure;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication(scanBasePackages = "com.energiaclara")
@ConfigurationPropertiesScan(basePackages = "com.energiaclara.infrastructure")
public class EnergiaClaraApplication {
    public static void main(String[] args) {
        SpringApplication.run(EnergiaClaraApplication.class, args);
    }
}
