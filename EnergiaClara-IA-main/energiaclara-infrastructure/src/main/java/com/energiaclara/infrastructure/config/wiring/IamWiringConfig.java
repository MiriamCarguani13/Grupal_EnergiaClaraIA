package com.energiaclara.infrastructure.config.wiring;

import com.energiaclara.iam.application.port.IamUserRepositoryPort;
import com.energiaclara.iam.application.port.PasswordHasherPort;
import com.energiaclara.iam.application.port.TokenIssuerPort;
import com.energiaclara.iam.application.service.IamApplicationService;
import com.energiaclara.iam.application.usecase.LoginUseCase;
import com.energiaclara.iam.application.usecase.RegisterUserUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IamWiringConfig {

    @Bean
    public IamApplicationService iamApplicationService(IamUserRepositoryPort userRepository,
                                                       PasswordHasherPort passwordHasher,
                                                       TokenIssuerPort tokenIssuer) {
        return new IamApplicationService(userRepository, passwordHasher, tokenIssuer);
    }

    @Bean
    public LoginUseCase loginUseCase(IamApplicationService service) {
        return service;
    }

    @Bean
    public RegisterUserUseCase registerUserUseCase(IamApplicationService service) {
        return service;
    }
}
