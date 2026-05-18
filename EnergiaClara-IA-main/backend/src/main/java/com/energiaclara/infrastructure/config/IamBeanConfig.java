package com.energiaclara.infrastructure.config;

import com.energiaclara.iam.application.port.IamUserRepositoryPort;
import com.energiaclara.iam.application.port.PasswordHasherPort;
import com.energiaclara.iam.application.port.TokenIssuerPort;
import com.energiaclara.iam.application.service.IamApplicationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IamBeanConfig {
    @Bean
    public IamApplicationService iamApplicationService(IamUserRepositoryPort userRepository, PasswordHasherPort passwordHasher, TokenIssuerPort tokenIssuer) {
        return new IamApplicationService(userRepository, passwordHasher, tokenIssuer);
    }
}
