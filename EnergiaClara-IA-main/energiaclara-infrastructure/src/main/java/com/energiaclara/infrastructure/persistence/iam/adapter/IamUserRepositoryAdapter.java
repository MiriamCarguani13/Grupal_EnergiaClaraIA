package com.energiaclara.infrastructure.persistence.iam.adapter;

import com.energiaclara.core.domain.shared.DomainException;
import com.energiaclara.core.domain.shared.Email;
import com.energiaclara.core.domain.shared.TenantId;
import com.energiaclara.core.domain.shared.UserId;
import com.energiaclara.iam.application.port.IamUserRepositoryPort;
import com.energiaclara.iam.domain.IamUser;
import com.energiaclara.iam.domain.Role;
import com.energiaclara.infrastructure.persistence.iam.entity.RolEntity;
import com.energiaclara.infrastructure.persistence.iam.entity.UsuarioEntity;
import com.energiaclara.infrastructure.persistence.iam.entity.UsuarioRolEntity;
import com.energiaclara.infrastructure.persistence.iam.repository.RolRepository;
import com.energiaclara.infrastructure.persistence.iam.repository.UsuarioRepository;
import com.energiaclara.infrastructure.persistence.iam.repository.UsuarioRolRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
public class IamUserRepositoryAdapter implements IamUserRepositoryPort {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioRolRepository usuarioRolRepository;
    private final RolRepository rolRepository;

    public IamUserRepositoryAdapter(UsuarioRepository usuarioRepository,
                                    UsuarioRolRepository usuarioRolRepository,
                                    RolRepository rolRepository) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioRolRepository = usuarioRolRepository;
        this.rolRepository = rolRepository;
    }

    @Override
    @Transactional
    public IamUser save(IamUser user) {
        UUID inquilinoId = user.getTenantId().value();
        UUID usuarioId = user.getId().value();

        UsuarioEntity entity = new UsuarioEntity();
        entity.setUsuarioId(usuarioId);
        entity.setInquilinoId(inquilinoId);
        entity.setCorreo(user.getEmail().value());
        entity.setNombreCompleto(user.getFullName());
        entity.setContrasenaHash(user.getHashedPassword());
        entity.setEstaActivo(user.isActive());
        entity.setCreadoEn(user.getCreatedAt());
        entity.setActualizadoEn(Instant.now());
        usuarioRepository.save(entity);

        Instant now = Instant.now();
        for (Role role : user.getRoles()) {
            RolEntity rol = rolRepository.findByNombre(role.name())
                    .orElseThrow(() -> new DomainException("Rol no encontrado en DB: " + role.name()));
            UsuarioRolEntity link = new UsuarioRolEntity();
            link.setUsuarioRolId(UUID.randomUUID());
            link.setUsuarioId(usuarioId);
            link.setRolId(rol.getRolId());
            link.setInquilinoId(inquilinoId);
            link.setAsignadoEl(now);
            link.setAsignadoPor(usuarioId);
            usuarioRolRepository.save(link);
        }

        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<IamUser> findByEmailAndTenantId(Email email, TenantId tenantId) {
        return usuarioRepository.findByCorreoAndInquilinoId(email.value(), tenantId.value())
                .map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmailAndTenantId(Email email, TenantId tenantId) {
        return usuarioRepository.existsByCorreoAndInquilinoId(email.value(), tenantId.value());
    }

    private IamUser toDomain(UsuarioEntity entity) {
        List<UsuarioRolEntity> links = usuarioRolRepository
                .findAllByUsuarioIdAndInquilinoId(entity.getUsuarioId(), entity.getInquilinoId());
        Set<Role> roles = new HashSet<>();
        for (UsuarioRolEntity link : links) {
            rolRepository.findById(link.getRolId()).ifPresent(rol -> {
                try {
                    roles.add(Role.valueOf(rol.getNombre()));
                } catch (IllegalArgumentException ignored) {
                    // Nombre de rol en DB no mapea al enum — ignorar
                }
            });
        }
        if (roles.isEmpty()) {
            throw new DomainException("Usuario sin roles asignados: " + entity.getCorreo());
        }

        return IamUser.hydrate(
                UserId.of(entity.getUsuarioId()),
                TenantId.of(entity.getInquilinoId()),
                new Email(entity.getCorreo()),
                entity.getNombreCompleto(),
                entity.getContrasenaHash(),
                roles,
                entity.isEstaActivo(),
                entity.getCreadoEn()
        );
    }
}
