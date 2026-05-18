package com.energiaclara.application.service;

import com.energiaclara.application.dto.LoginCommand;
import com.energiaclara.application.dto.LoginResult;
import com.energiaclara.application.dto.RegisterUserCommand;
import com.energiaclara.application.port.in.LoginUseCase;
import com.energiaclara.application.port.in.RegisterUserUseCase;
import com.energiaclara.application.port.out.TokenPort;
import com.energiaclara.domain.model.User;
import com.energiaclara.domain.model.vo.UserId;
import com.energiaclara.domain.port.out.UserRepositoryPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthApplicationService implements LoginUseCase, RegisterUserUseCase {

    private final UserRepositoryPort userRepository;
    private final TokenPort tokenPort;
    private final PasswordEncoder passwordEncoder;

    public AuthApplicationService(UserRepositoryPort userRepository,
                                  TokenPort tokenPort,
                                  PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenPort = tokenPort;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResult login(LoginCommand command) {
        User user = userRepository.findByEmailAndTenantId(command.email(), command.tenantId())
                .orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas"));

        if (!user.isActive()) {
            throw new IllegalStateException("Usuario inactivo");
        }

        if (!passwordEncoder.matches(command.password(), user.getHashedPassword())) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }

        String token = tokenPort.generateToken(user);
        return new LoginResult(token, user.getId().toString(), user.getTenantId().toString(), user.getRoles());
    }

    @Override
    @Transactional
    public UserId register(RegisterUserCommand command) {
        if (userRepository.existsByEmailAndTenantId(command.email(), command.tenantId())) {
            throw new IllegalArgumentException("Email ya registrado en esta institución");
        }

        String hashed = passwordEncoder.encode(command.rawPassword());
        User user = User.create(
                command.tenantId(),
                command.email(),
                command.fullName(),
                hashed,
                command.roles()
        );
        userRepository.save(user, command.assignedBy());
        return user.getId();
    }
}
