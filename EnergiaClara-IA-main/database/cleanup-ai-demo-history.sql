/*
  Limpieza segura de historial demo que afecta Dashboard/Anomalias e IA hibrida.

  NO ejecutar automaticamente. Revisar las consultas de verificacion al final.

  Alcance de borrado:
  - Lecturas demo de MED-DEMO-001 del 2026-06-03 y 2026-06-04.
  - Anomalias demo asociadas a esas lecturas o al mismo medidor/fechas/areas.
  - Tickets demo vinculados a esas anomalias, si existen.
  - Resumen analitico / KPI diario / log de prediccion IA asociado a esas fechas.

  Conserva:
  - Usuarios, roles, tenant, tecnicos.
  - Medidores, edificios, configuracion.
  - Baselines validos y snapshot_linea_base.
  - Datos fuera de MED-DEMO-001 o fuera del 03/04-jun-2026.
*/

USE EnergiaClaraDB;
GO

SET NOCOUNT ON;
SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    DECLARE @tenantId UNIQUEIDENTIFIER = '11111111-1111-1111-1111-111111111111';
    DECLARE @meterCode NVARCHAR(80) = N'MED-DEMO-001';
    DECLARE @medidorId UNIQUEIDENTIFIER;
    DECLARE @startDate DATE = '2026-06-03';
    DECLARE @endDate DATE = '2026-06-04';

    SELECT @medidorId = medidor_id
    FROM [core].[medidor]
    WHERE inquilino_id = @tenantId
      AND codigo_medidor = @meterCode;

    IF @medidorId IS NULL
    BEGIN
        THROW 51000, 'No existe el medidor demo MED-DEMO-001 para el tenant esperado.', 1;
    END;

    DECLARE @readingsToDelete TABLE (
        lectura_id UNIQUEIDENTIFIER PRIMARY KEY
    );

    DECLARE @anomaliesToDelete TABLE (
        anomalia_id UNIQUEIDENTIFIER PRIMARY KEY
    );

    DECLARE @ticketsToDelete TABLE (
        ticket_id UNIQUEIDENTIFIER PRIMARY KEY
    );

    /*
      Lecturas demo que afectan el aprendizaje/historial visible.
      El criterio combina tenant + medidor + fecha exacta + senales demo.
    */
    INSERT INTO @readingsToDelete (lectura_id)
    SELECT DISTINCT l.lectura_id
    FROM [consumo].[lectura] l
    WHERE l.inquilino_id = @tenantId
      AND l.medidor_id = @medidorId
      AND (
            l.fecha_lectura BETWEEN @startDate AND @endDate
            OR CAST(l.periodo_inicio AS DATE) BETWEEN @startDate AND @endDate
            OR CAST(l.periodo_fin AS DATE) BETWEEN @startDate AND @endDate
            OR CAST(l.creado_en AS DATE) BETWEEN @startDate AND @endDate
      )
      AND (
            l.meter_label = @meterCode
            OR l.facility_label IN (N'Sede Central', N'Bloque B', N'Aula 3B', N'Bloque B - Aula 3B')
            OR l.facility_label LIKE N'%Sede Central%'
            OR l.facility_label LIKE N'%Bloque B%'
            OR l.facility_label LIKE N'%Aula 3B%'
      );

    /*
      Anomalias demo visibles o historicas asociadas al mismo rango.
      Incluye las que se originaron en lecturas demo y las que quedaron sin lectura
      pero con medidor/fecha/area demo.
    */
    INSERT INTO @anomaliesToDelete (anomalia_id)
    SELECT DISTINCT a.anomalia_id
    FROM [energiaops].[anomalia] a
    WHERE a.inquilino_id = @tenantId
      AND a.medidor_id = @medidorId
      AND (
            EXISTS (
                SELECT 1
                FROM @readingsToDelete r
                WHERE r.lectura_id = a.lectura_id
            )
            OR CAST(a.detectada_el AS DATE) BETWEEN @startDate AND @endDate
      )
      AND (
            a.estado IN ('DETECTADA', 'IGNORADA', 'DERIVADA', 'EN_ATENCION', 'RESUELTA')
            OR a.ticket_id IS NOT NULL
      )
      AND (
            a.meter_label = @meterCode
            OR a.facility_label IN (N'Sede Central', N'Bloque B', N'Aula 3B', N'Bloque B - Aula 3B')
            OR a.facility_label LIKE N'%Sede Central%'
            OR a.facility_label LIKE N'%Bloque B%'
            OR a.facility_label LIKE N'%Aula 3B%'
            OR a.explicacion LIKE N'%baseline%'
            OR a.explicacion LIKE N'%Z-Score%'
            OR a.version_modelo_ia = N'hybrid-stat-rules-v1.0'
      );

    /*
      Tickets demo relacionados con las anomalias que se eliminaran.
      Se borran para respetar fk_ticket_anomalia antes de eliminar energiaops.anomalia.
    */
    INSERT INTO @ticketsToDelete (ticket_id)
    SELECT DISTINCT t.ticket_id
    FROM [mantenimiento].[ticket] t
    INNER JOIN @anomaliesToDelete a ON a.anomalia_id = t.anomalia_id
    WHERE t.inquilino_id = @tenantId;

    INSERT INTO @ticketsToDelete (ticket_id)
    SELECT DISTINCT t.ticket_id
    FROM [mantenimiento].[ticket] t
    INNER JOIN [energiaops].[anomalia] a ON a.ticket_id = t.ticket_id
    INNER JOIN @anomaliesToDelete d ON d.anomalia_id = a.anomalia_id
    WHERE t.inquilino_id = @tenantId
      AND NOT EXISTS (
          SELECT 1
          FROM @ticketsToDelete existing
          WHERE existing.ticket_id = t.ticket_id
      );

    /*
      Orden por dependencias:
      1. Resumen/impacto y tablas hijas de ticket.
      2. Tickets.
      3. Logs/KPIs directos del rango demo.
      4. Anomalias.
      5. Lecturas.
    */

    IF OBJECT_ID('analitica.resumen_impacto', 'U') IS NOT NULL
    BEGIN
        DELETE ri
        FROM [analitica].[resumen_impacto] ri
        WHERE ri.inquilino_id = @tenantId
          AND (
                EXISTS (SELECT 1 FROM @anomaliesToDelete a WHERE a.anomalia_id = ri.anomalia_id)
                OR EXISTS (SELECT 1 FROM @ticketsToDelete t WHERE t.ticket_id = ri.ticket_id)
          );
    END;

    IF OBJECT_ID('mantenimiento.evidencia_ticket', 'U') IS NOT NULL
    BEGIN
        DELETE e
        FROM [mantenimiento].[evidencia_ticket] e
        INNER JOIN @ticketsToDelete d ON d.ticket_id = e.ticket_id
        WHERE e.inquilino_id = @tenantId;
    END;

    IF OBJECT_ID('mantenimiento.historial_asignacion_ticket', 'U') IS NOT NULL
    BEGIN
        DELETE h
        FROM [mantenimiento].[historial_asignacion_ticket] h
        INNER JOIN @ticketsToDelete d ON d.ticket_id = h.ticket_id
        WHERE h.inquilino_id = @tenantId;
    END;

    IF OBJECT_ID('mantenimiento.lista_verificacion_ticket', 'U') IS NOT NULL
    BEGIN
        DELETE l
        FROM [mantenimiento].[lista_verificacion_ticket] l
        INNER JOIN @ticketsToDelete d ON d.ticket_id = l.ticket_id
        WHERE l.inquilino_id = @tenantId;
    END;

    DELETE t
    FROM [mantenimiento].[ticket] t
    INNER JOIN @ticketsToDelete d ON d.ticket_id = t.ticket_id
    WHERE t.inquilino_id = @tenantId;

    IF OBJECT_ID('energiaops.log_prediccion_ia', 'U') IS NOT NULL
    BEGIN
        DELETE lp
        FROM [energiaops].[log_prediccion_ia] lp
        WHERE lp.inquilino_id = @tenantId
          AND lp.medidor_id = @medidorId
          AND CAST(lp.llamada_el AS DATE) BETWEEN @startDate AND @endDate;
    END;

    IF OBJECT_ID('analitica.kpi_diario', 'U') IS NOT NULL
    BEGIN
        DELETE kd
        FROM [analitica].[kpi_diario] kd
        WHERE kd.inquilino_id = @tenantId
          AND kd.medidor_id = @medidorId
          AND kd.fecha_kpi BETWEEN @startDate AND @endDate;
    END;

    /*
      No se borra analitica.kpi_mensual porque no depende directamente de una lectura
      y puede mezclar datos reales de todo el mes.
      No se borra energiaops.snapshot_linea_base ni baselines validos.
    */

    DELETE a
    FROM [energiaops].[anomalia] a
    INNER JOIN @anomaliesToDelete d ON d.anomalia_id = a.anomalia_id
    WHERE a.inquilino_id = @tenantId;

    DELETE l
    FROM [consumo].[lectura] l
    INNER JOIN @readingsToDelete d ON d.lectura_id = l.lectura_id
    WHERE l.inquilino_id = @tenantId
      AND NOT EXISTS (
            SELECT 1
            FROM [energiaops].[anomalia] a
            WHERE a.lectura_id = l.lectura_id
      );

    DECLARE @readingsDeleted INT = @@ROWCOUNT;
    DECLARE @anomaliesSelected INT = (SELECT COUNT(1) FROM @anomaliesToDelete);
    DECLARE @ticketsSelected INT = (SELECT COUNT(1) FROM @ticketsToDelete);

    COMMIT TRANSACTION;

    PRINT CONCAT('Lecturas demo eliminadas: ', @readingsDeleted);
    PRINT CONCAT('Anomalias demo seleccionadas/eliminadas: ', @anomaliesSelected);
    PRINT CONCAT('Tickets demo vinculados seleccionados/eliminados: ', @ticketsSelected);
    PRINT 'Conservados: usuarios, roles, tenant, medidores, edificios, tecnicos, configuracion y baselines.';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;

    THROW;
END CATCH;
GO

/*
  Consultas de verificacion despues de ejecutar:

  DECLARE @tenantId UNIQUEIDENTIFIER = '11111111-1111-1111-1111-111111111111';
  DECLARE @meterCode NVARCHAR(80) = N'MED-DEMO-001';
  DECLARE @medidorId UNIQUEIDENTIFIER;

  SELECT @medidorId = medidor_id
  FROM [core].[medidor]
  WHERE inquilino_id = @tenantId
    AND codigo_medidor = @meterCode;

  -- Total lecturas restantes del medidor demo
  SELECT COUNT(*) AS total_lecturas_restantes
  FROM [consumo].[lectura]
  WHERE inquilino_id = @tenantId
    AND medidor_id = @medidorId;

  -- Lecturas restantes especificamente de 03/04-jun-2026
  SELECT COUNT(*) AS lecturas_demo_03_04_jun_restantes
  FROM [consumo].[lectura]
  WHERE inquilino_id = @tenantId
    AND medidor_id = @medidorId
    AND fecha_lectura BETWEEN '2026-06-03' AND '2026-06-04';

  -- Total anomalias restantes del medidor demo
  SELECT COUNT(*) AS total_anomalias_restantes
  FROM [energiaops].[anomalia]
  WHERE inquilino_id = @tenantId
    AND medidor_id = @medidorId;

  -- Anomalias activas restantes visibles en Analytics
  SELECT COUNT(*) AS anomalias_activas_restantes
  FROM [energiaops].[anomalia]
  WHERE inquilino_id = @tenantId
    AND medidor_id = @medidorId
    AND estado IN ('DETECTADA', 'DERIVADA', 'EN_ATENCION', 'RESUELTA');

  -- Tickets restantes vinculados al medidor/anomalias demo
  SELECT COUNT(*) AS tickets_restantes_vinculados
  FROM [mantenimiento].[ticket] t
  INNER JOIN [energiaops].[anomalia] a ON a.anomalia_id = t.anomalia_id
  WHERE t.inquilino_id = @tenantId
    AND a.medidor_id = @medidorId;

  -- KPI diario restante de esas fechas
  SELECT COUNT(*) AS kpi_diario_03_04_jun_restante
  FROM [analitica].[kpi_diario]
  WHERE inquilino_id = @tenantId
    AND medidor_id = @medidorId
    AND fecha_kpi BETWEEN '2026-06-03' AND '2026-06-04';

  -- Baselines/snapshots conservados
  SELECT COUNT(*) AS snapshot_linea_base_conservados
  FROM [energiaops].[snapshot_linea_base]
  WHERE inquilino_id = @tenantId
    AND medidor_id = @medidorId;
*/
