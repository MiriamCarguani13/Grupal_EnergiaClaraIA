/*
  EnergiaClara AI - Reset seguro de datos demo EnergyOps/Analytics

  Objetivo:
  - Limpiar datos operativos/demo del medidor MED-DEMO-001.
  - Conservar usuarios, roles, tenant, configuracion de seguridad y datos maestros.
  - Asegurar medidor demo y baseline demo para probar IA hibrida por API.

  Importante:
  - Ejecutar despues de database/script.sql y database/seeds.sql.
  - Este script NO inserta lecturas ni anomalias falsas.
  - Las 10 lecturas de prueba deben enviarse por POST /api/energyops/analyze-reading
    usando database/demo-energy-readings-payloads.md para que el flujo real genere
    baseline dinamico, explicacion, recomendaciones, anomalias, dashboard y analytics.
*/

USE EnergiaClaraDB;
GO

SET NOCOUNT ON;
SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    DECLARE @tenantId UNIQUEIDENTIFIER = '11111111-1111-1111-1111-111111111111';
    DECLARE @buildingId UNIQUEIDENTIFIER = '22222222-2222-2222-2222-222222222222';
    DECLARE @meterId UNIQUEIDENTIFIER = '33333333-3333-3333-3333-333333333333';
    DECLARE @meterCode NVARCHAR(50) = N'MED-DEMO-001';
    DECLARE @facilityLabel NVARCHAR(80) = N'Sede Central - Bloque B - Aula 3B';

    IF NOT EXISTS (SELECT 1 FROM [core].[inquilino] WHERE inquilino_id = @tenantId)
    BEGIN
        THROW 52000, 'No existe el tenant demo. Ejecuta database/seeds.sql antes de este reset.', 1;
    END;

    IF NOT EXISTS (SELECT 1 FROM [core].[edificio] WHERE edificio_id = @buildingId)
    BEGIN
        INSERT INTO [core].[edificio]
            (edificio_id, inquilino_id, nombre, direccion, ciudad, tipo_edificio, esta_activo, creado_en)
        VALUES
            (@buildingId, @tenantId, N'Edificio Principal Demo', N'Av. Demo 123', N'La Paz', N'ACADEMICO', 1, SYSUTCDATETIME());
    END;

    IF NOT EXISTS (
        SELECT 1
        FROM [core].[medidor]
        WHERE inquilino_id = @tenantId
          AND codigo_medidor = @meterCode
    )
    BEGIN
        INSERT INTO [core].[medidor]
            (medidor_id, inquilino_id, edificio_id, codigo_medidor, tipo_medidor, unidad,
             descripcion_ubicacion, esta_activo, instalado_el, creado_en, actualizado_en)
        VALUES
            (@meterId, @tenantId, @buildingId, @meterCode, N'ELECTRICIDAD', N'kWh',
             N'Sede Central / Bloque B / Aula 3B', 1, CAST(SYSUTCDATETIME() AS DATE), SYSUTCDATETIME(), SYSUTCDATETIME());
    END
    ELSE
    BEGIN
        SELECT @meterId = medidor_id
        FROM [core].[medidor]
        WHERE inquilino_id = @tenantId
          AND codigo_medidor = @meterCode;

        UPDATE [core].[medidor]
        SET esta_activo = 1,
            edificio_id = COALESCE(edificio_id, @buildingId),
            descripcion_ubicacion = COALESCE(descripcion_ubicacion, N'Sede Central / Bloque B / Aula 3B'),
            actualizado_en = SYSUTCDATETIME()
        WHERE medidor_id = @meterId
          AND inquilino_id = @tenantId;
    END;

    DECLARE @readingsToDelete TABLE (lectura_id UNIQUEIDENTIFIER PRIMARY KEY);
    DECLARE @anomaliesToDelete TABLE (anomalia_id UNIQUEIDENTIFIER PRIMARY KEY);
    DECLARE @ticketsToDelete TABLE (ticket_id UNIQUEIDENTIFIER PRIMARY KEY);
    DECLARE @auditEventsToDelete TABLE (evento_id UNIQUEIDENTIFIER PRIMARY KEY);

    INSERT INTO @readingsToDelete (lectura_id)
    SELECT l.lectura_id
    FROM [consumo].[lectura] l
    WHERE l.inquilino_id = @tenantId
      AND l.medidor_id = @meterId;

    INSERT INTO @anomaliesToDelete (anomalia_id)
    SELECT DISTINCT a.anomalia_id
    FROM [energiaops].[anomalia] a
    WHERE a.inquilino_id = @tenantId
      AND (
            a.medidor_id = @meterId
            OR EXISTS (SELECT 1 FROM @readingsToDelete r WHERE r.lectura_id = a.lectura_id)
      );

    INSERT INTO @ticketsToDelete (ticket_id)
    SELECT DISTINCT t.ticket_id
    FROM [mantenimiento].[ticket] t
    INNER JOIN @anomaliesToDelete a ON a.anomalia_id = t.anomalia_id
    WHERE t.inquilino_id = @tenantId;

    INSERT INTO @ticketsToDelete (ticket_id)
    SELECT DISTINCT t.ticket_id
    FROM [mantenimiento].[ticket] t
    WHERE t.inquilino_id = @tenantId
      AND (
            t.titulo LIKE N'%DEMO%'
            OR t.titulo LIKE N'%anomal%'
            OR t.descripcion LIKE N'%MED-DEMO-001%'
            OR t.descripcion LIKE N'%Aula 3B%'
          )
      AND NOT EXISTS (SELECT 1 FROM @ticketsToDelete d WHERE d.ticket_id = t.ticket_id);

    INSERT INTO @auditEventsToDelete (evento_id)
    SELECT e.evento_id
    FROM [audit].[evento_auditoria] e
    WHERE e.inquilino_id = @tenantId
      AND (
            e.endpoint LIKE N'%/api/energyops/%'
            OR e.endpoint LIKE N'%/api/analytics/%'
            OR e.endpoint LIKE N'%/api/maintenance/tickets%'
            OR e.tipo_recurso IN (N'EnergyReading', N'EnergyAnomaly', N'LecturaEnergetica', N'AnomaliaEnergetica', N'MaintenanceTicket')
            OR EXISTS (SELECT 1 FROM @readingsToDelete r WHERE CONVERT(NVARCHAR(50), r.lectura_id) = e.recurso_id)
            OR EXISTS (SELECT 1 FROM @anomaliesToDelete a WHERE CONVERT(NVARCHAR(50), a.anomalia_id) = e.recurso_id)
            OR EXISTS (SELECT 1 FROM @ticketsToDelete t WHERE CONVERT(NVARCHAR(50), t.ticket_id) = e.recurso_id)
      );

    DELETE lc
    FROM [audit].[log_cambios] lc
    INNER JOIN @auditEventsToDelete e ON e.evento_id = lc.evento_id;

    DELETE e
    FROM [audit].[evento_auditoria] e
    INNER JOIN @auditEventsToDelete d ON d.evento_id = e.evento_id;

    IF OBJECT_ID('mantenimiento.evidencia_ticket', 'U') IS NOT NULL
    BEGIN
        DELETE et
        FROM [mantenimiento].[evidencia_ticket] et
        INNER JOIN @ticketsToDelete t ON t.ticket_id = et.ticket_id
        WHERE et.inquilino_id = @tenantId;
    END;

    IF OBJECT_ID('mantenimiento.historial_asignacion_ticket', 'U') IS NOT NULL
    BEGIN
        DELETE ht
        FROM [mantenimiento].[historial_asignacion_ticket] ht
        INNER JOIN @ticketsToDelete t ON t.ticket_id = ht.ticket_id
        WHERE ht.inquilino_id = @tenantId;
    END;

    IF OBJECT_ID('mantenimiento.lista_verificacion_ticket', 'U') IS NOT NULL
    BEGIN
        DELETE lv
        FROM [mantenimiento].[lista_verificacion_ticket] lv
        INNER JOIN @ticketsToDelete t ON t.ticket_id = lv.ticket_id
        WHERE lv.inquilino_id = @tenantId;
    END;

    DELETE ri
    FROM [analitica].[resumen_impacto] ri
    LEFT JOIN @anomaliesToDelete a ON a.anomalia_id = ri.anomalia_id
    LEFT JOIN @ticketsToDelete t ON t.ticket_id = ri.ticket_id
    WHERE ri.inquilino_id = @tenantId
      AND (a.anomalia_id IS NOT NULL OR t.ticket_id IS NOT NULL);

    DELETE kd
    FROM [analitica].[kpi_diario] kd
    WHERE kd.inquilino_id = @tenantId
      AND kd.medidor_id = @meterId;

    DELETE lp
    FROM [energiaops].[log_prediccion_ia] lp
    WHERE lp.inquilino_id = @tenantId
      AND lp.medidor_id = @meterId;

    IF COL_LENGTH('energiaops.anomalia', 'ticket_id') IS NOT NULL
    BEGIN
        UPDATE a
        SET ticket_id = NULL
        FROM [energiaops].[anomalia] a
        INNER JOIN @ticketsToDelete t ON t.ticket_id = a.ticket_id
        WHERE a.inquilino_id = @tenantId;
    END;

    DELETE t
    FROM [mantenimiento].[ticket] t
    INNER JOIN @ticketsToDelete d ON d.ticket_id = t.ticket_id
    WHERE t.inquilino_id = @tenantId;

    DELETE a
    FROM [energiaops].[anomalia] a
    INNER JOIN @anomaliesToDelete d ON d.anomalia_id = a.anomalia_id
    WHERE a.inquilino_id = @tenantId;

    DELETE l
    FROM [consumo].[lectura] l
    INNER JOIN @readingsToDelete d ON d.lectura_id = l.lectura_id
    WHERE l.inquilino_id = @tenantId;

    IF COL_LENGTH('energiaops.snapshot_linea_base', 'activo') IS NOT NULL
    BEGIN
        UPDATE [energiaops].[snapshot_linea_base]
        SET activo = 0
        WHERE inquilino_id = @tenantId
          AND medidor_id = @meterId;
    END;

    INSERT INTO [energiaops].[snapshot_linea_base]
        (linea_base_id, inquilino_id, medidor_id, tipo_periodo, referencia_inicio, referencia_fin,
         valor_promedio, valor_p95, desviacion_estandar, conteo_muestras, calculado_el,
         tolerancia_porcentaje, activo, facility_label, meter_label)
    VALUES
        (NEWID(), @tenantId, @meterId, N'DIARIO',
         DATEADD(DAY, -30, CAST(SYSUTCDATETIME() AS DATE)),
         CAST(SYSUTCDATETIME() AS DATE),
         100.0000, 115.0000, 7.5000, 30, SYSUTCDATETIME(),
         15.000, 1, @facilityLabel, @meterCode);

    COMMIT TRANSACTION;

    PRINT 'Reset demo EnergyOps/Analytics completado.';
    PRINT 'No se eliminaron usuarios, roles, tenants ni configuracion de seguridad.';
    PRINT 'No se insertaron lecturas ni anomalias falsas. Envia los payloads por API para activar la IA hibrida real.';

    SELECT 'lecturas_restantes_medidor_demo' AS verificacion, COUNT(*) AS total
    FROM [consumo].[lectura]
    WHERE inquilino_id = @tenantId AND medidor_id = @meterId;

    SELECT 'anomalias_restantes_medidor_demo' AS verificacion, COUNT(*) AS total
    FROM [energiaops].[anomalia]
    WHERE inquilino_id = @tenantId AND medidor_id = @meterId;

    SELECT 'tickets_demo_restantes' AS verificacion, COUNT(*) AS total
    FROM [mantenimiento].[ticket]
    WHERE inquilino_id = @tenantId
      AND (
            titulo LIKE N'%DEMO%'
            OR titulo LIKE N'%anomal%'
            OR descripcion LIKE N'%MED-DEMO-001%'
            OR descripcion LIKE N'%Aula 3B%'
          );

    SELECT TOP 1
        'baseline_activo_medidor_demo' AS verificacion,
        valor_promedio,
        tolerancia_porcentaje,
        activo,
        facility_label,
        meter_label
    FROM [energiaops].[snapshot_linea_base]
    WHERE inquilino_id = @tenantId
      AND medidor_id = @meterId
      AND activo = 1
    ORDER BY calculado_el DESC;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;

    THROW;
END CATCH;
GO
