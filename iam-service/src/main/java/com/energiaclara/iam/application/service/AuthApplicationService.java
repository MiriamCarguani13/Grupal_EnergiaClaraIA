package com.energiaclara.iam.application.service;

import com.energiaclara.core.model.vo.TenantId;
import com.energiaclara.core.model.vo.UserId;
import com.energiaclara.iam.application.dto.LoginCommand;
import com.energiaclara.iam.application.dto.LoginResult;
import com.energiaclara.iam.application.dto.RegisterUserCommand;
import com.energiaclara.iam.application.port.in.LoginUseCase;
import com.energiaclara.iam.application.port.in.RegisterUserUseCase;
import com.energiaclara.iam.application.port.out.PasswordHasherPort;
import com.energiaclara.iam.application.port.out.TokenPort;
import com.energiaclara.iam.application.port.out.UserRepositoryPort;
import com.energiaclara.iam.domain.model.Role;
import com.energiaclara.iam.domain.model.User;
import com.energiaclara.iam.domain.model.vo.Email;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthApplicationService implements LoginUseCase, RegisterUserUseCase {

    private final UserRepositoryPort userRepository;
    private final TokenPort tokenPort;
    private final PasswordHasherPort passwordHasher;

    public AuthApplicationService(UserRepositoryPort userRepository,
                                  TokenPort tokenPort,
                                  PasswordHasherPort passwordHasher) {
        this.userRepository = userRepository;
        this.tokenPort = tokenPort;
        this.passwordHasher = passwordHasher;
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResult login(LoginCommand command) {
        User user = userRepository.findByEmailAndTenantId(Email.of(command.email()), TenantId.of(command.tenantId()))
                .orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas"));

        if (!user.isActive()) {
            throw new IllegalStateException("Usuario inactivo");
        }

        if (!passwordHasher.matches(command.password(), user.getHashedPassword())) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }

        String token = tokenPort.generateToken(user);
        return new LoginResult(token, user.getId().toString(), user.getTenantId().toString(), toRoleNames(user.getRoles()));
    }

    @Override
    @Transactional
    public String register(RegisterUserCommand command) {
        TenantId tenantId = TenantId.of(command.tenantId());
        Email email = Email.of(command.email());
        if (userRepository.existsByEmailAndTenantId(email, tenantId)) {
            throw new IllegalArgumentException("Email ya registrado en esta institución");
        }

        String hashed = passwordHasher.hash(command.rawPassword());
        User user = User.create(
                tenantId,
                email,
                command.fullName(),
                hashed,
                toRoles(command.roles())
        );
        userRepository.save(user, UserId.of(command.assignedBy()));
        return user.getId().toString();
    }

    private Set<Role> toRoles(Set<String> roles) {
        return roles.stream()
                .map(Role::valueOf)
                .collect(Collectors.toUnmodifiableSet());
    }

    private Set<String> toRoleNames(Set<Role> roles) {
        return roles.stream()
                .map(Role::name)
                .collect(Collectors.toUnmodifiableSet());
    }
}
