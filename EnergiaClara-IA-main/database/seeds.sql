USE EnergiaClaraDB;
GO

-- Limpiar datos previos (en orden de dependencias)
DELETE FROM [energiaops].[snapshot_linea_base];
DELETE FROM [core].[medidor];
DELETE FROM [core].[edificio];
DELETE FROM [iam].[usuario_rol];
DELETE FROM [iam].[usuario];
DELETE FROM [core].[inquilino];
GO

-- Reinsertar con valores CORRECTOS
DECLARE @tenantId    UNIQUEIDENTIFIER = '11111111-1111-1111-1111-111111111111';
DECLARE @edificioId  UNIQUEIDENTIFIER = '22222222-2222-2222-2222-222222222222';
DECLARE @medidorId   UNIQUEIDENTIFIER = '33333333-3333-3333-3333-333333333333';
DECLARE @adminId     UNIQUEIDENTIFIER = '44444444-4444-4444-4444-444444444444';

-- ✅ Tenant con tipo_plan VÁLIDO (BASIC, PRO, ENTERPRISE)
INSERT INTO [core].[inquilino]
  (inquilino_id, nombre, nombre_legal, nit_rut, tipo_plan, factor_co2, codigo_moneda, esta_activo, creado_en, actualizado_en)
VALUES
  (@tenantId, 'Instituto Tecnológico Demo', 'Instituto Tecnológico Demo SA', '0000000000', 'BASIC',
   0.250000, 'BOB', 1, SYSUTCDATETIME(), SYSUTCDATETIME());

-- ✅ Admin
INSERT INTO [iam].[usuario]
  (usuario_id, inquilino_id, correo, nombre_completo, contrasena_hash, esta_activo, creado_en, actualizado_en)
VALUES
  (@adminId, @tenantId, 'admin@demo.edu', 'Administrador Demo',
   '$2a$10$kdFT40lwlms9N5VJiQ7ES.4a2it/uhBEGlZco19apZw3Y/3CIgmQW',
   1, SYSUTCDATETIME(), SYSUTCDATETIME());

-- ✅ Asignar rol ADMIN_INSTITUCION al admin
INSERT INTO [iam].[usuario_rol]
  (usuario_rol_id, usuario_id, rol_id, inquilino_id, edificio_id, asignado_el, asignado_por)
SELECT NEWID(), @adminId, rol_id, @tenantId, NULL, SYSUTCDATETIME(), @adminId
FROM [iam].[rol] WHERE nombre = 'ADMIN_INSTITUCION';

-- ✅ Edificio demo
INSERT INTO [core].[edificio]
  (edificio_id, inquilino_id, nombre, direccion, ciudad, tipo_edificio, esta_activo, creado_en)
VALUES
  (@edificioId, @tenantId, 'Edificio Principal Demo', 'Av. Demo 123', 'La Paz', 'ACADEMICO', 1, SYSUTCDATETIME());

-- ✅ Medidor demo con tipo_medidor VÁLIDO (ELECTRICIDAD, AGUA, GAS)
INSERT INTO [core].[medidor]
  (medidor_id, inquilino_id, edificio_id, codigo_medidor, tipo_medidor, unidad, descripcion_ubicacion, esta_activo, instalado_el, creado_en, actualizado_en)
VALUES
  (@medidorId, @tenantId, @edificioId, 'MED-DEMO-001', 'ELECTRICIDAD', 'kWh', 'Tablero principal demo', 1,
   CAST(SYSUTCDATETIME() AS DATE), SYSUTCDATETIME(), SYSUTCDATETIME());

-- ✅ Línea base demo
INSERT INTO [energiaops].[snapshot_linea_base]
  (linea_base_id, inquilino_id, medidor_id, tipo_periodo, referencia_inicio, referencia_fin,
   valor_promedio, valor_p95, desviacion_estandar, conteo_muestras, calculado_el,
   tolerancia_porcentaje, activo, facility_label, meter_label)
VALUES
  (NEWID(), @tenantId, @medidorId, 'DIARIO',
   DATEADD(DAY, -30, CAST(SYSUTCDATETIME() AS DATE)),
   CAST(SYSUTCDATETIME() AS DATE),
   100.000, 130.000, 12.500, 30, SYSUTCDATETIME(),
   15.000, 1, 'EDIFICIO-PRINCIPAL', 'MED-DEMO-001');

PRINT '✅ Seeds aplicados correctamente con valores válidos.';
GO