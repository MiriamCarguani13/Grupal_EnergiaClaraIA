-- ============================================================
-- EnergíaClara AI — Seeds mínimos + extensiones de columnas
-- Ejecutar DESPUÉS de database/script.sql
-- ============================================================

USE EnergiaClaraDB;
GO

-- ────────────────────────────────────────────────────────────
-- 1. Catálogo de roles (el backend mapea estos nombres a su enum)
-- ────────────────────────────────────────────────────────────
IF NOT EXISTS (SELECT 1 FROM [iam].[rol] WHERE nombre = 'ADMIN_INSTITUCION')
    INSERT INTO [iam].[rol] (rol_id, nombre, descripcion, nivel_alcance)
    VALUES (NEWID(), 'ADMIN_INSTITUCION', 'Configurador global de la institución', 'INSTITUCION');

IF NOT EXISTS (SELECT 1 FROM [iam].[rol] WHERE nombre = 'DIRECTOR')
    INSERT INTO [iam].[rol] (rol_id, nombre, descripcion, nivel_alcance)
    VALUES (NEWID(), 'DIRECTOR', 'Supervisor de KPIs', 'INSTITUCION');

IF NOT EXISTS (SELECT 1 FROM [iam].[rol] WHERE nombre = 'DOCENTE')
    INSERT INTO [iam].[rol] (rol_id, nombre, descripcion, nivel_alcance)
    VALUES (NEWID(), 'DOCENTE', 'Gestor cultural / retos', 'EDIFICIO');

IF NOT EXISTS (SELECT 1 FROM [iam].[rol] WHERE nombre = 'ESTUDIANTE')
    INSERT INTO [iam].[rol] (rol_id, nombre, descripcion, nivel_alcance)
    VALUES (NEWID(), 'ESTUDIANTE', 'Participante operativo', 'EDIFICIO');

IF NOT EXISTS (SELECT 1 FROM [iam].[rol] WHERE nombre = 'TECNICO')
    INSERT INTO [iam].[rol] (rol_id, nombre, descripcion, nivel_alcance)
    VALUES (NEWID(), 'TECNICO', 'Ejecutor de mantenimiento', 'EDIFICIO');

IF NOT EXISTS (SELECT 1 FROM [iam].[rol] WHERE nombre = 'AUDITOR')
    INSERT INTO [iam].[rol] (rol_id, nombre, descripcion, nivel_alcance)
    VALUES (NEWID(), 'AUDITOR', 'Validador de cumplimiento', 'INSTITUCION');
GO

-- ────────────────────────────────────────────────────────────
-- 2. IDs demo (UUIDs fijos — el backend los referencia por config)
-- ────────────────────────────────────────────────────────────
DECLARE @tenantId    UNIQUEIDENTIFIER = '11111111-1111-1111-1111-111111111111';
DECLARE @edificioId  UNIQUEIDENTIFIER = '22222222-2222-2222-2222-222222222222';
DECLARE @medidorId   UNIQUEIDENTIFIER = '33333333-3333-3333-3333-333333333333';
DECLARE @adminId     UNIQUEIDENTIFIER = '44444444-4444-4444-4444-444444444444';
DECLARE @tecnicoId   UNIQUEIDENTIFIER = '55555555-5555-5555-5555-555555555555';

-- Tenant
IF NOT EXISTS (SELECT 1 FROM [core].[inquilino] WHERE inquilino_id = @tenantId)
BEGIN
    INSERT INTO [core].[inquilino]
      (inquilino_id, nombre, nombre_legal, nit_rut, tipo_plan, factor_co2, codigo_moneda, esta_activo, creado_en, actualizado_en)
    VALUES
      (@tenantId, 'Instituto Tecnológico Demo', 'Instituto Tecnológico Demo SA', '0000000000', 'BASIC',
       0.250000, 'BOB', 1, SYSUTCDATETIME(), SYSUTCDATETIME());
END

-- Admin (UUID fijo para que el backend pueda referenciarlo)
IF NOT EXISTS (SELECT 1 FROM [iam].[usuario] WHERE usuario_id = @adminId)
BEGIN
    INSERT INTO [iam].[usuario]
      (usuario_id, inquilino_id, correo, nombre_completo, contrasena_hash, esta_activo, creado_en, actualizado_en)
    VALUES
      (@adminId, @tenantId, 'admin@demo.edu', 'Administrador Demo',
       '$2a$10$kdFT40lwlms9N5VJiQ7ES.4a2it/uhBEGlZco19apZw3Y/3CIgmQW',
       1, SYSUTCDATETIME(), SYSUTCDATETIME());

    INSERT INTO [iam].[usuario_rol]
      (usuario_rol_id, usuario_id, rol_id, inquilino_id, edificio_id, asignado_el, asignado_por)
    SELECT NEWID(), @adminId, rol_id, @tenantId, NULL, SYSUTCDATETIME(), @adminId
    FROM [iam].[rol] WHERE nombre = 'ADMIN_INSTITUCION';
END

-- Tecnico demo para mantenimiento
IF NOT EXISTS (SELECT 1 FROM [iam].[usuario] WHERE usuario_id = @tecnicoId)
BEGIN
    INSERT INTO [iam].[usuario]
      (usuario_id, inquilino_id, correo, nombre_completo, contrasena_hash, esta_activo, creado_en, actualizado_en)
    VALUES
      (@tecnicoId, @tenantId, 'tecnico@demo.edu', 'Tecnico Demo',
       '$2a$10$wXrY9v5V0cMmuCVib3xkv.QS7h.QSoGEthllvyVlDokZycgAGC6J2',
       1, SYSUTCDATETIME(), SYSUTCDATETIME());
END
ELSE
BEGIN
    UPDATE [iam].[usuario]
    SET inquilino_id = @tenantId,
        correo = 'tecnico@demo.edu',
        contrasena_hash = '$2a$10$wXrY9v5V0cMmuCVib3xkv.QS7h.QSoGEthllvyVlDokZycgAGC6J2',
        esta_activo = 1,
        actualizado_en = SYSUTCDATETIME()
    WHERE usuario_id = @tecnicoId;
END

IF NOT EXISTS (
    SELECT 1
    FROM [iam].[usuario_rol] ur
    INNER JOIN [iam].[rol] r ON r.rol_id = ur.rol_id
    WHERE ur.usuario_id = @tecnicoId
      AND ur.inquilino_id = @tenantId
      AND r.nombre = 'TECNICO'
)
BEGIN
    INSERT INTO [iam].[usuario_rol]
      (usuario_rol_id, usuario_id, rol_id, inquilino_id, edificio_id, asignado_el, asignado_por)
    SELECT NEWID(), @tecnicoId, rol_id, @tenantId, @edificioId, SYSUTCDATETIME(), @adminId
    FROM [iam].[rol] WHERE nombre = 'TECNICO';
END

-- Edificio demo
IF NOT EXISTS (SELECT 1 FROM [core].[edificio] WHERE edificio_id = @edificioId)
BEGIN
    INSERT INTO [core].[edificio]
      (edificio_id, inquilino_id, nombre, direccion, ciudad, tipo_edificio, esta_activo, creado_en)
    VALUES
      (@edificioId, @tenantId, 'Edificio Principal Demo', 'Av. Demo 123', 'La Paz', 'ACADEMICO', 1, SYSUTCDATETIME());
END

-- Medidor demo
IF NOT EXISTS (SELECT 1 FROM [core].[medidor] WHERE medidor_id = @medidorId)
BEGIN
    INSERT INTO [core].[medidor]
      (medidor_id, inquilino_id, edificio_id, codigo_medidor, tipo_medidor, unidad, descripcion_ubicacion, esta_activo, instalado_el, creado_en, actualizado_en)
    VALUES
      (@medidorId, @tenantId, @edificioId, 'MED-DEMO-001', 'ELECTRICIDAD', 'kWh', 'Tablero principal demo', 1,
       CAST(SYSUTCDATETIME() AS DATE), SYSUTCDATETIME(), SYSUTCDATETIME());
END
GO

-- ────────────────────────────────────────────────────────────
-- 3. Extensiones de columnas para el módulo EnergyOps
--    (cost/co2/recomendación que no existen en el schema canónico)
-- ────────────────────────────────────────────────────────────

-- [consumo].[lectura]: etiquetas legibles + métricas eléctricas auxiliares
IF COL_LENGTH('consumo.lectura', 'facility_label') IS NULL
    ALTER TABLE [consumo].[lectura] ADD [facility_label] NVARCHAR(80) NULL;
IF COL_LENGTH('consumo.lectura', 'meter_label') IS NULL
    ALTER TABLE [consumo].[lectura] ADD [meter_label] NVARCHAR(80) NULL;
IF COL_LENGTH('consumo.lectura', 'voltaje') IS NULL
    ALTER TABLE [consumo].[lectura] ADD [voltaje] DECIMAL(10,3) NULL;
IF COL_LENGTH('consumo.lectura', 'factor_potencia') IS NULL
    ALTER TABLE [consumo].[lectura] ADD [factor_potencia] DECIMAL(5,3) NULL;
GO

-- [energiaops].[snapshot_linea_base]: tolerancia + flag activo + etiquetas
IF COL_LENGTH('energiaops.snapshot_linea_base', 'tolerancia_porcentaje') IS NULL
    ALTER TABLE [energiaops].[snapshot_linea_base] ADD [tolerancia_porcentaje] DECIMAL(7,3) NULL;
IF COL_LENGTH('energiaops.snapshot_linea_base', 'activo') IS NULL
    ALTER TABLE [energiaops].[snapshot_linea_base] ADD [activo] BIT NOT NULL CONSTRAINT df_snapshot_lb_activo DEFAULT (1);
IF COL_LENGTH('energiaops.snapshot_linea_base', 'facility_label') IS NULL
    ALTER TABLE [energiaops].[snapshot_linea_base] ADD [facility_label] NVARCHAR(80) NULL;
IF COL_LENGTH('energiaops.snapshot_linea_base', 'meter_label') IS NULL
    ALTER TABLE [energiaops].[snapshot_linea_base] ADD [meter_label] NVARCHAR(80) NULL;
GO

-- [energiaops].[anomalia]: recomendación textual + costo + co2 + etiquetas
IF COL_LENGTH('energiaops.anomalia', 'recomendacion') IS NULL
    ALTER TABLE [energiaops].[anomalia] ADD [recomendacion] NVARCHAR(700) NULL;
IF COL_LENGTH('energiaops.anomalia', 'costo_estimado') IS NULL
    ALTER TABLE [energiaops].[anomalia] ADD [costo_estimado] DECIMAL(12,2) NULL;
IF COL_LENGTH('energiaops.anomalia', 'co2_estimado') IS NULL
    ALTER TABLE [energiaops].[anomalia] ADD [co2_estimado] DECIMAL(12,2) NULL;
IF COL_LENGTH('energiaops.anomalia', 'facility_label') IS NULL
    ALTER TABLE [energiaops].[anomalia] ADD [facility_label] NVARCHAR(80) NULL;
IF COL_LENGTH('energiaops.anomalia', 'meter_label') IS NULL
    ALTER TABLE [energiaops].[anomalia] ADD [meter_label] NVARCHAR(80) NULL;
GO

-- [audit].[evento_auditoria]: metadata HTTP + status + duración (módulo de Paulo)
IF COL_LENGTH('audit.evento_auditoria', 'metodo_http') IS NULL
    ALTER TABLE [audit].[evento_auditoria] ADD [metodo_http] NVARCHAR(10) NULL;
IF COL_LENGTH('audit.evento_auditoria', 'endpoint') IS NULL
    ALTER TABLE [audit].[evento_auditoria] ADD [endpoint] NVARCHAR(500) NULL;
IF COL_LENGTH('audit.evento_auditoria', 'user_email') IS NULL
    ALTER TABLE [audit].[evento_auditoria] ADD [user_email] NVARCHAR(255) NULL;
IF COL_LENGTH('audit.evento_auditoria', 'user_agent') IS NULL
    ALTER TABLE [audit].[evento_auditoria] ADD [user_agent] NVARCHAR(500) NULL;
IF COL_LENGTH('audit.evento_auditoria', 'estado') IS NULL
    ALTER TABLE [audit].[evento_auditoria] ADD [estado] NVARCHAR(20) NULL;
IF COL_LENGTH('audit.evento_auditoria', 'mensaje_error') IS NULL
    ALTER TABLE [audit].[evento_auditoria] ADD [mensaje_error] NVARCHAR(MAX) NULL;
IF COL_LENGTH('audit.evento_auditoria', 'duracion_ms') IS NULL
    ALTER TABLE [audit].[evento_auditoria] ADD [duracion_ms] BIGINT NULL;
GO

-- [mantenimiento].[ticket]: ficha tecnica editable por rol TECNICO
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

-- ────────────────────────────────────────────────────────────
-- 4. Línea base demo activa para el medidor demo
-- ────────────────────────────────────────────────────────────
DECLARE @tenantId2 UNIQUEIDENTIFIER = '11111111-1111-1111-1111-111111111111';
DECLARE @medidorId2 UNIQUEIDENTIFIER = '33333333-3333-3333-3333-333333333333';

IF NOT EXISTS (SELECT 1 FROM [energiaops].[snapshot_linea_base]
               WHERE medidor_id = @medidorId2 AND activo = 1)
BEGIN
    INSERT INTO [energiaops].[snapshot_linea_base]
      (linea_base_id, inquilino_id, medidor_id, tipo_periodo, referencia_inicio, referencia_fin,
       valor_promedio, valor_p95, desviacion_estandar, conteo_muestras, calculado_el,
       tolerancia_porcentaje, activo, facility_label, meter_label)
    VALUES
      (NEWID(), @tenantId2, @medidorId2, 'DIARIO',
       DATEADD(DAY, -30, CAST(SYSUTCDATETIME() AS DATE)),
       CAST(SYSUTCDATETIME() AS DATE),
       100.000, 130.000, 12.500, 30, SYSUTCDATETIME(),
       15.000, 1, 'EDIFICIO-PRINCIPAL', 'MED-DEMO-001');
END
GO

PRINT 'Seeds aplicados correctamente.';
