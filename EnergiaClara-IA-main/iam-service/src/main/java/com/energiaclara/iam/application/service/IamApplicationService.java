package com.energiaclara.iam.application.service;


import com.energiaclara.core.domain.shared.DomainException;
import com.energiaclara.core.domain.shared.UserId;
import com.energiaclara.iam.application.dto.LoginCommand;
import com.energiaclara.iam.application.dto.LoginResult;
import com.energiaclara.iam.application.dto.RegisterUserCommand;
import com.energiaclara.iam.application.port.IamUserRepositoryPort;
import com.energiaclara.iam.application.port.PasswordHasherPort;
import com.energiaclara.iam.application.port.TokenIssuerPort;
import com.energiaclara.iam.application.usecase.LoginUseCase;
import com.energiaclara.iam.application.usecase.RegisterUserUseCase;
import com.energiaclara.iam.domain.AuthenticatedPrincipal;
import com.energiaclara.iam.domain.IamUser;

public class IamApplicationService implements LoginUseCase, RegisterUserUseCase {
    private final IamUserRepositoryPort userRepository;
    private final PasswordHasherPort passwordHasher;
    private final TokenIssuerPort tokenIssuer;

    public IamApplicationService(IamUserRepositoryPort userRepository, PasswordHasherPort passwordHasher, TokenIssuerPort tokenIssuer) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.tokenIssuer = tokenIssuer;
    }

    @Override
    public LoginResult login(LoginCommand command) {
        IamUser user = userRepository.findByEmailAndTenantId(command.email(), command.tenantId())
                .orElseThrow(() -> new DomainException("Credenciales inválidas"));
        if (!passwordHasher.matches(command.rawPassword(), user.getHashedPassword())) {
            throw new DomainException("Credenciales inválidas");
        }
        AuthenticatedPrincipal principal = user.authenticate();
        String token = tokenIssuer.issueToken(principal);
        return new LoginResult(token, principal.userId(), principal.tenantId(), principal.roles());
    }

    @Override
    public UserId register(RegisterUserCommand command) {
        if (userRepository.existsByEmailAndTenantId(command.email(), command.tenantId())) {
            throw new DomainException("Email ya registrado en esta institución");
        }
        String hashedPassword = passwordHasher.hash(command.rawPassword());
        IamUser user = IamUser.register(command.tenantId(), command.email(), command.fullName(), hashedPassword, command.roles());
        userRepository.save(user);
        return user.getId();
    }
}