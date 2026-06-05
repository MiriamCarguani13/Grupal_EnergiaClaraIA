/*
  Seed/migracion minima para acceso TECNICO y ficha tecnica de tickets.

  Ejecutar despues de database/script.sql. Es idempotente y conserva usuarios/roles existentes.
*/

USE EnergiaClaraDB;
GO

SET NOCOUNT ON;
SET XACT_ABORT ON;

IF COL_LENGTH('mantenimiento.ticket', 'diagnostico_tecnico') IS NULL
    ALTER TABLE [mantenimiento].[ticket] ADD [diagnostico_tecnico] NVARCHAR(MAX) NULL;
IF COL_LENGTH('mantenimiento.ticket', 'solucion_aplicada') IS NULL
    ALTER TABLE [mantenimiento].[ticket] ADD [solucion_aplicada] NVARCHAR(MAX) NULL;
IF COL_LENGTH('mantenimiento.ticket', 'materiales_utilizados') IS NULL
    ALTER TABLE [mantenimiento].[ticket] ADD [materiales_utilizados] NVARCHAR(MAX) NULL;
IF COL_LENGTH('mantenimiento.ticket', 'observaciones_tecnicas') IS NULL
    ALTER TABLE [mantenimiento].[ticket] ADD [observaciones_tecnicas] NVARCHAR(MAX) NULL;
IF COL_LENGTH('mantenimiento.ticket', 'estado_tecnico') IS NULL
    ALTER TABLE [mantenimiento].[ticket] ADD [estado_tecnico] NVARCHAR(30) NULL;
IF COL_LENGTH('mantenimiento.ticket', 'atendido_el') IS NULL
    ALTER TABLE [mantenimiento].[ticket] ADD [atendido_el] DATETIME2(3) NULL;
IF COL_LENGTH('mantenimiento.ticket', 'tecnico_responsable_id') IS NULL
    ALTER TABLE [mantenimiento].[ticket] ADD [tecnico_responsable_id] UNIQUEIDENTIFIER NULL;
IF COL_LENGTH('mantenimiento.ticket', 'evidencia_imagen_url') IS NULL
    ALTER TABLE [mantenimiento].[ticket] ADD [evidencia_imagen_url] NVARCHAR(1000) NULL;

IF COL_LENGTH('energiaops.anomalia', 'ticket_id') IS NULL
    ALTER TABLE [energiaops].[anomalia] ADD [ticket_id] UNIQUEIDENTIFIER NULL;
IF COL_LENGTH('energiaops.anomalia', 'tecnico_responsable_id') IS NULL
    ALTER TABLE [energiaops].[anomalia] ADD [tecnico_responsable_id] UNIQUEIDENTIFIER NULL;
IF COL_LENGTH('energiaops.anomalia', 'tecnico_responsable_nombre') IS NULL
    ALTER TABLE [energiaops].[anomalia] ADD [tecnico_responsable_nombre] NVARCHAR(200) NULL;

IF EXISTS (
    SELECT 1
    FROM sys.check_constraints
    WHERE name = 'chk_anomalia_estado'
      AND parent_object_id = OBJECT_ID('energiaops.anomalia')
)
    ALTER TABLE [energiaops].[anomalia] DROP CONSTRAINT [chk_anomalia_estado];

ALTER TABLE [energiaops].[anomalia] WITH CHECK ADD CONSTRAINT [chk_anomalia_estado]
CHECK ([estado] IN ('DETECTADA', 'DERIVADA', 'EN_ATENCION', 'RESUELTA', 'IGNORADA', 'NOTIFICADA', 'EN_ACCION'));

IF EXISTS (
    SELECT 1
    FROM sys.check_constraints
    WHERE name = 'chk_ticket_estado'
      AND parent_object_id = OBJECT_ID('mantenimiento.ticket')
)
    ALTER TABLE [mantenimiento].[ticket] DROP CONSTRAINT [chk_ticket_estado];

ALTER TABLE [mantenimiento].[ticket] WITH CHECK ADD CONSTRAINT [chk_ticket_estado]
CHECK ([estado] IN ('PENDIENTE', 'EN_PROGRESO', 'REPARADO', 'CERRADO', 'REABIERTO', 'ASIGNADO', 'ABIERTO', 'BORRADOR'));
GO

BEGIN TRY
    BEGIN TRANSACTION;

    DECLARE @tenantId UNIQUEIDENTIFIER = '11111111-1111-1111-1111-111111111111';
    DECLARE @edificioId UNIQUEIDENTIFIER = '22222222-2222-2222-2222-222222222222';
    DECLARE @adminId UNIQUEIDENTIFIER = '44444444-4444-4444-4444-444444444444';
    DECLARE @tecnicoId UNIQUEIDENTIFIER = '55555555-5555-5555-5555-555555555555';
    DECLARE @slaId UNIQUEIDENTIFIER = '66666666-6666-6666-6666-666666666666';
    DECLARE @ticketId UNIQUEIDENTIFIER = '77777777-7777-7777-7777-777777777777';
    DECLARE @tecnicoHash NVARCHAR(500) = '$2a$10$wXrY9v5V0cMmuCVib3xkv.QS7h.QSoGEthllvyVlDokZycgAGC6J2';
    DECLARE @tecnicoRoleId UNIQUEIDENTIFIER;

    IF NOT EXISTS (SELECT 1 FROM [iam].[rol] WHERE nombre = 'TECNICO')
    BEGIN
        INSERT INTO [iam].[rol] (rol_id, nombre, descripcion, nivel_alcance)
        VALUES (NEWID(), 'TECNICO', 'Ejecutor de mantenimiento', 'EDIFICIO');
    END;

    SELECT @tecnicoRoleId = rol_id
    FROM [iam].[rol]
    WHERE nombre = 'TECNICO';

    IF NOT EXISTS (SELECT 1 FROM [iam].[usuario] WHERE usuario_id = @tecnicoId)
    BEGIN
        INSERT INTO [iam].[usuario]
          (usuario_id, inquilino_id, correo, nombre_completo, contrasena_hash, esta_activo, creado_en, actualizado_en)
        VALUES
          (@tecnicoId, @tenantId, 'tecnico@demo.edu', 'Tecnico Demo', @tecnicoHash, 1, SYSUTCDATETIME(), SYSUTCDATETIME());
    END
    ELSE
    BEGIN
        UPDATE [iam].[usuario]
        SET inquilino_id = @tenantId,
            correo = 'tecnico@demo.edu',
            contrasena_hash = @tecnicoHash,
            esta_activo = 1,
            actualizado_en = SYSUTCDATETIME()
        WHERE usuario_id = @tecnicoId;
    END;

    IF NOT EXISTS (
        SELECT 1
        FROM [iam].[usuario_rol]
        WHERE usuario_id = @tecnicoId
          AND rol_id = @tecnicoRoleId
          AND inquilino_id = @tenantId
    )
    BEGIN
        INSERT INTO [iam].[usuario_rol]
          (usuario_rol_id, usuario_id, rol_id, inquilino_id, edificio_id, asignado_el, asignado_por)
        VALUES (NEWID(), @tecnicoId, @tecnicoRoleId, @tenantId, @edificioId, SYSUTCDATETIME(), @adminId);
    END;

    IF NOT EXISTS (SELECT 1 FROM [mantenimiento].[politica_sla] WHERE politica_sla_id = @slaId)
    BEGIN
        INSERT INTO [mantenimiento].[politica_sla]
          (politica_sla_id, inquilino_id, nombre, severidad, horas_respuesta, horas_resolucion, esta_activa, creado_en)
        VALUES
          (@slaId, @tenantId, 'SLA demo tecnico', 'ALTA', 4, 24, 1, SYSUTCDATETIME());
    END;

    IF NOT EXISTS (SELECT 1 FROM [mantenimiento].[ticket] WHERE ticket_id = @ticketId)
    BEGIN
        INSERT INTO [mantenimiento].[ticket]
          (ticket_id, inquilino_id, edificio_id, anomalia_id, politica_sla_id,
           titulo, descripcion, prioridad, estado, asignado_a, creado_por,
           vencimiento_sla, sla_incumplido, cerrado_el, cerrado_por,
           conteo_reapertura, creado_en, actualizado_en,
           diagnostico_tecnico, solucion_aplicada, materiales_utilizados,
           observaciones_tecnicas, estado_tecnico, atendido_el, tecnico_responsable_id)
        VALUES
          (@ticketId, @tenantId, @edificioId, NULL, @slaId,
           'Revision electrica Aula 3B', 'Verificar consumo anomalo en sistema A/C e iluminacion.',
           'ALTA', 'PENDIENTE', @tecnicoId, @adminId,
           DATEADD(HOUR, 24, SYSUTCDATETIME()), 0, NULL, NULL,
           0, SYSUTCDATETIME(), SYSUTCDATETIME(),
           NULL, NULL, NULL, NULL, NULL, NULL, NULL);
    END
    ELSE
    BEGIN
        UPDATE [mantenimiento].[ticket]
        SET asignado_a = @tecnicoId,
            estado = CASE WHEN estado IN ('BORRADOR', 'ABIERTO', 'ASIGNADO') THEN 'PENDIENTE' ELSE estado END,
            actualizado_en = SYSUTCDATETIME()
        WHERE ticket_id = @ticketId;
    END;

    COMMIT TRANSACTION;

    PRINT 'Seed tecnico/mantenimiento aplicado correctamente.';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;

    THROW;
END CATCH;
GO
