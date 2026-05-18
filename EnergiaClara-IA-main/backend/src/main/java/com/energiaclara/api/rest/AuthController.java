package com.energiaclara.api.rest;

import com.energiaclara.api.rest.audit.Audited;
import com.energiaclara.api.rest.dto.LoginRequest;
import com.energiaclara.api.rest.dto.LoginResponse;
import com.energiaclara.api.rest.dto.RegisterRequest;
import com.energiaclara.core.domain.shared.Email;
import com.energiaclara.core.domain.shared.TenantId;
import com.energiaclara.core.domain.shared.UserId;
import com.energiaclara.iam.application.dto.LoginCommand;
import com.energiaclara.iam.application.dto.LoginResult;
import com.energiaclara.iam.application.dto.RegisterUserCommand;
import com.energiaclara.iam.application.usecase.LoginUseCase;
import com.energiaclara.iam.application.usecase.RegisterUserUseCase;
import com.energiaclara.infrastructure.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final RegisterUserUseCase registerUserUseCase;

    public AuthController(LoginUseCase loginUseCase, RegisterUserUseCase registerUserUseCase) {
        this.loginUseCase = loginUseCase;
        this.registerUserUseCase = registerUserUseCase;
    }

    @PostMapping("/login")
    @Audited(action = "LOGIN", entity = "User")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginCommand command = new LoginCommand(
                new Email(request.email()),
                request.password(),
                TenantId.of(request.tenantId())
        );
        LoginResult result = loginUseCase.login(command);
        return ResponseEntity.ok(new LoginResponse(
                result.token(), result.userId().toString(), result.tenantId().toString(), result.roles()
        ));
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN_INSTITUCION')")
    @Audited(action = "REGISTER_USER", entity = "User", entityIdExpression = "#result.body['userId']")
    public ResponseEntity<Map<String, String>> register(
            @Valid @RequestBody RegisterRequest request,
            @AuthenticationPrincipal AuthenticatedUser current) {
        RegisterUserCommand command = new RegisterUserCommand(
                TenantId.of(request.tenantId()),
                new Email(request.email()),
                request.fullName(),
                request.password(),
                request.roles(),
                UserId.of(current.userId())
        );
        UserId userId = registerUserUseCase.register(command);
        return ResponseEntity.ok(Map.of("userId", userId.toString()));
    }
}
