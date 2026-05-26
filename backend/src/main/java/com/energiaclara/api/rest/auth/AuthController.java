package com.energiaclara.api.rest.auth;

import com.energiaclara.api.rest.audit.Audited;
import com.energiaclara.api.rest.dto.LoginRequest;
import com.energiaclara.api.rest.dto.LoginResponse;
import com.energiaclara.api.rest.dto.RegisterRequest;
import com.energiaclara.application.dto.LoginCommand;
import com.energiaclara.application.dto.LoginResult;
import com.energiaclara.application.dto.RegisterUserCommand;
import com.energiaclara.application.port.in.LoginUseCase;
import com.energiaclara.application.port.in.RegisterUserUseCase;
import com.energiaclara.application.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final RegisterUserUseCase registerUserUseCase;
    private final com.energiaclara.application.port.out.UserRepositoryPort userRepositoryPort;

    public AuthController(LoginUseCase loginUseCase, RegisterUserUseCase registerUserUseCase, com.energiaclara.application.port.out.UserRepositoryPort userRepositoryPort) {
        this.loginUseCase = loginUseCase;
        this.registerUserUseCase = registerUserUseCase;
        this.userRepositoryPort = userRepositoryPort;
    }

    @PostMapping("/login")
    @Audited(action = "LOGIN", entity = "User")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginCommand command = new LoginCommand(
                request.email(),
                request.password(),
                request.tenantId()
        );
        LoginResult result = loginUseCase.login(command);
        return ResponseEntity.ok(new LoginResponse(
                result.token(), result.userId(), result.tenantId(), result.roles()
        ));
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN_INSTITUCION')")
    @Audited(action = "REGISTER_USER", entity = "User", entityIdExpression = "#result.body['userId']")
    public ResponseEntity<Map<String, String>> register(
            @Valid @RequestBody RegisterRequest request,
            @AuthenticationPrincipal AuthenticatedUser current) {
        RegisterUserCommand command = new RegisterUserCommand(
                request.tenantId(),
                request.email(),
                request.fullName(),
                request.password(),
                request.roles(),
                current.userId().toString()
        );
        String userId = registerUserUseCase.register(command);
        return ResponseEntity.ok(Map.of("userId", userId.toString()));
    }

    @org.springframework.web.bind.annotation.GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN_INSTITUCION')")
    public ResponseEntity<java.util.List<Map<String, String>>> getUsersByRole(
            @org.springframework.web.bind.annotation.RequestParam("role") String role,
            @AuthenticationPrincipal AuthenticatedUser current) {
        
        java.util.List<com.energiaclara.domain.model.User> users = userRepositoryPort.findByRoleAndTenantId(role, com.energiaclara.domain.model.vo.TenantId.of(current.tenantId()));
        
        java.util.List<Map<String, String>> response = users.stream().map(u -> Map.of(
                "id", u.getId().value().toString(),
                "fullName", u.getFullName(),
                "email", u.getEmail().value()
        )).toList();
        
        return ResponseEntity.ok(response);
    }
}
