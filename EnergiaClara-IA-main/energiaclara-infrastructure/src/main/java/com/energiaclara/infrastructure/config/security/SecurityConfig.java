package com.energiaclara.infrastructure.config.security;

import com.energiaclara.iam.application.port.TokenVerifierPort;
import com.energiaclara.iam.domain.AuthenticatedPrincipal;
import com.energiaclara.iam.domain.Role;
import com.energiaclara.infrastructure.api.exception.ApiErrorResponse;
import com.energiaclara.infrastructure.api.exception.ErrorCodes;
import com.energiaclara.infrastructure.config.ratelimit.InMemoryRateLimitingFilter;
import com.energiaclara.infrastructure.security.context.CorrelationIdContext;
import com.energiaclara.infrastructure.security.context.CorrelationIdFilter;
import com.energiaclara.infrastructure.security.context.TenantContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    @ConditionalOnProperty(prefix = "security.jwt", name = "enabled", havingValue = "true", matchIfMissing = true)
    public SecurityFilterChain jwtSecurityFilterChain(HttpSecurity http,
                                                      @Qualifier("corsConfigurationSource") CorsConfigurationSource corsSource,
                                                      CorrelationIdFilter correlationIdFilter,
                                                      InMemoryRateLimitingFilter rateLimitFilter,
                                                      TokenVerifierPort tokenVerifier,
                                                      ObjectMapper objectMapper) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(c -> c.configurationSource(corsSource))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/register-public",
                                "/actuator/health",
                                "/actuator/info",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/error"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(correlationIdFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(rateLimitFilter, CorrelationIdFilter.class)
                .addFilterBefore(new JwtAuthenticationFilter(tokenVerifier, objectMapper), UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(eh -> eh
                        .authenticationEntryPoint((req, res, ex) -> writeError(req, res, objectMapper, HttpStatus.UNAUTHORIZED, ErrorCodes.AUTH_FAILED, "No autenticado"))
                        .accessDeniedHandler((req, res, ex) -> writeError(req, res, objectMapper, HttpStatus.FORBIDDEN, ErrorCodes.FORBIDDEN, "Acceso denegado"))
                );
        return http.build();
    }

    @Bean
    @ConditionalOnProperty(prefix = "security.jwt", name = "enabled", havingValue = "false")
    public SecurityFilterChain devSecurityFilterChain(HttpSecurity http,
                                                      @Qualifier("corsConfigurationSource") CorsConfigurationSource corsSource,
                                                      CorrelationIdFilter correlationIdFilter) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(c -> c.configurationSource(corsSource))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .addFilterBefore(correlationIdFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    private static void writeError(HttpServletRequest req, HttpServletResponse res, ObjectMapper om,
                                   HttpStatus status, String code, String message) throws IOException {
        res.setStatus(status.value());
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiErrorResponse body = ApiErrorResponse.of(code, message, req.getRequestURI(), CorrelationIdContext.get());
        om.writeValue(res.getOutputStream(), body);
    }

    static class JwtAuthenticationFilter extends OncePerRequestFilter {
        private final TokenVerifierPort verifier;
        private final ObjectMapper objectMapper;

        JwtAuthenticationFilter(TokenVerifierPort verifier, ObjectMapper objectMapper) {
            this.verifier = verifier;
            this.objectMapper = objectMapper;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                throws ServletException, IOException {
            String auth = request.getHeader("Authorization");
            if (auth != null && auth.startsWith("Bearer ")) {
                String token = auth.substring(7);
                try {
                    AuthenticatedPrincipal principal = verifier.verify(token);
                    Collection<SimpleGrantedAuthority> authorities = principal.roles().stream()
                            .map(Role::name)
                            .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                            .collect(Collectors.toSet());
                    PrincipalAuthentication authentication = new PrincipalAuthentication(principal, authorities);
                    authentication.setAuthenticated(true);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    TenantContext.set(principal.tenantId().value());
                } catch (RuntimeException ex) {
                    SecurityContextHolder.clearContext();
                    writeError(request, response, objectMapper, HttpStatus.UNAUTHORIZED, ErrorCodes.AUTH_FAILED, "Token inválido o expirado");
                    return;
                }
            }
            try {
                chain.doFilter(request, response);
            } finally {
                TenantContext.clear();
            }
        }
    }

    public static class PrincipalAuthentication extends AbstractAuthenticationToken {
        private final AuthenticatedPrincipal principal;

        public PrincipalAuthentication(AuthenticatedPrincipal principal, Collection<SimpleGrantedAuthority> authorities) {
            super(authorities);
            this.principal = principal;
        }

        @Override public Object getCredentials() { return ""; }
        @Override public Object getPrincipal() { return principal; }
        public AuthenticatedPrincipal getAuthenticatedPrincipal() { return principal; }
    }
}
