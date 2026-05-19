package com.energiaclara.infrastructure.api.rest;

import com.energiaclara.core.domain.shared.DomainException;
import com.energiaclara.core.domain.shared.Email;
import com.energiaclara.core.domain.shared.TenantId;
import com.energiaclara.core.domain.shared.UserId;
import com.energiaclara.iam.application.dto.LoginCommand;
import com.energiaclara.iam.application.dto.LoginResult;
import com.energiaclara.iam.application.dto.RegisterUserCommand;
import com.energiaclara.iam.application.usecase.LoginUseCase;
import com.energiaclara.iam.application.usecase.RegisterUserUseCase;
import com.energiaclara.iam.domain.AuthenticatedPrincipal;
import com.energiaclara.iam.domain.Role;
import com.energiaclara.infrastructure.api.rest.dto.AuthTokenResponse;
import com.energiaclara.infrastructure.api.rest.dto.LoginRequestDto;
import com.energiaclara.infrastructure.api.rest.dto.RegisterUserRequestDto;
import com.energiaclara.infrastructure.api.rest.dto.UserResponse;
import com.energiaclara.infrastructure.audit.AuditTrailService;
import com.energiaclara.infrastructure.config.security.SecurityConfig;
import com.energiaclara.infrastructure.security.adapter.JwtTokenIssuerAdapter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Autenticación y registro de usuarios")
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final RegisterUserUseCase registerUserUseCase;
    private final AuditTrailService auditTrail;
    private final JwtTokenIssuerAdapter tokenIssuer;

    public AuthController(LoginUseCase loginUseCase,
                          RegisterUserUseCase registerUserUseCase,
                          AuditTrailService auditTrail,
                          JwtTokenIssuerAdapter tokenIssuer) {
        this.loginUseCase = loginUseCase;
        this.registerUserUseCase = registerUserUseCase;
        this.auditTrail = auditTrail;
        this.tokenIssuer = tokenIssuer;
    }

    @PostMapping("/login")
    @Operation(summary = "Login multi-tenant", description = "Retorna JWT access token")
    public ResponseEntity<AuthTokenResponse> login(@Valid @RequestBody LoginRequestDto req, HttpServletRequest http) {
        TenantId tenantId = TenantId.of(req.tenantId());
        Email email = new Email(req.email());

        try {
            LoginResult result = loginUseCase.login(new LoginCommand(email, req.password(), tenantId));
            auditTrail.record("AUTH_LOGIN_SUCCESS", "Usuario", result.userId().value().toString(),
                    tenantId.value(), result.userId().value(), "MEDIA", http.getRemoteAddr());

            List<String> roles = result.roles().stream().map(Role::name).toList();
            AuthTokenResponse body = new AuthTokenResponse(
                    result.token(),
                    "Bearer",
                    tokenIssuer.getAccessTokenSeconds(),
                    result.tenantId().value(),
                    result.userId().value(),
                    email.value(),
                    roles
            );
            return ResponseEntity.ok(body);
        } catch (DomainException ex) {
            auditTrail.record("AUTH_LOGIN_FAILED", "Usuario", email.value(),
                    tenantId.value(), tenantId.value(), "ALTA", http.getRemoteAddr());
            throw ex;
        }
    }

    @PostMapping("/register")
    @SecurityRequirement(name = "bearer-jwt")
    @PreAuthorize("hasRole('ADMIN_INSTITUCION')")
    @Operation(summary = "Crear usuario en el tenant", description = "Requiere rol ADMIN_INSTITUCION")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterUserRequestDto req, HttpServletRequest http) {
        AuthenticatedPrincipal caller = currentPrincipal();
        TenantId tenantId = TenantId.of(req.tenantId());

        if (!caller.tenantId().value().equals(tenantId.value())) {
            throw new DomainException("No autorizado para crear usuarios en otro tenant");
        }

        Set<Role> roles = new HashSet<>();
        for (String r : req.roles()) {
            try {
                roles.add(Role.valueOf(r));
            } catch (IllegalArgumentException ex) {
                throw new DomainException("Rol inválido: " + r);
            }
        }

        RegisterUserCommand cmd = new RegisterUserCommand(
                tenantId,
                new Email(req.email()),
                req.fullName(),
                req.password(),
                roles,
                caller.userId()
        );
        UserId newUserId = registerUserUseCase.register(cmd);
        auditTrail.record("USER_CREATED", "Usuario", newUserId.value().toString(),
                tenantId.value(), caller.userId().value(), "MEDIA", http.getRemoteAddr());

        UserResponse body = new UserResponse(
                newUserId.value(),
                tenantId.value(),
                req.email(),
                req.roles()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Perfil del usuario autenticado")
    public ResponseEntity<UserResponse> me() {
        AuthenticatedPrincipal p = currentPrincipal();
        return ResponseEntity.ok(new UserResponse(
                p.userId().value(),
                p.tenantId().value(),
                p.email().value(),
                p.roles().stream().map(Role::name).toList()
        ));
    }

    private AuthenticatedPrincipal currentPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof SecurityConfig.PrincipalAuthentication pa) {
            return pa.getAuthenticatedPrincipal();
        }
        throw new DomainException("No hay principal autenticado en el contexto");
    }
}
