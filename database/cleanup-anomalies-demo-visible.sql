/*
  Limpieza visual de anomalías demo pendientes.

  Objetivo:
  - Quitar de la pantalla principal anomalías demo pendientes sin borrar lecturas.
  - Conservar historial de IA híbrida, baselines, snapshots/KPIs y anomalías históricas.

  Decisión:
  - La constraint actual no permite ARCHIVADA ni DESCARTADA.
  - Sí permite IGNORADA, por lo que se usa como estado de archivo/descarte demo.
  - El backend filtra estados visibles a DETECTADA, DERIVADA, EN_ATENCION y RESUELTA.

  Alcance:
  - Solo tenant demo.
  - Solo medidor demo MED-DEMO-001.
  - Solo anomalías sin ticket asociado.
  - Solo estado DETECTADA.
  - No toca consumo.lectura.
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

    SELECT @medidorId = medidor_id
    FROM [core].[medidor]
    WHERE inquilino_id = @tenantId
      AND codigo_medidor = @meterCode;

    IF @medidorId IS NULL
    BEGIN
        THROW 51000, 'No existe el medidor demo MED-DEMO-001 para el tenant esperado.', 1;
    END;

    DECLARE @anomaliesToIgnore TABLE (
        anomalia_id UNIQUEIDENTIFIER PRIMARY KEY
    );

    /*
      Filtros seguros para "pendientes visibles" demo:
      - tenant demo + medidor demo
      - estado DETECTADA
      - sin ticket_id
      - señales demo en etiquetas o medidor exacto

      No se borra la anomalía; se marca como IGNORADA para preservar trazabilidad.
    */
    INSERT INTO @anomaliesToIgnore (anomalia_id)
    SELECT a.anomalia_id
    FROM [energiaops].[anomalia] a
    WHERE a.inquilino_id = @tenantId
      AND a.medidor_id = @medidorId
      AND a.estado = 'DETECTADA'
      AND (
            a.ticket_id IS NULL
            OR NOT EXISTS (
                SELECT 1
                FROM [mantenimiento].[ticket] t
                WHERE t.ticket_id = a.ticket_id
            )
      )
      AND (
            a.facility_label IN (N'Sede Central', N'Aula 3B', N'Bloque B - Aula 3B')
            OR a.meter_label = @meterCode
            OR a.explicacion LIKE N'%baseline%'
            OR a.explicacion LIKE N'%Z-Score%'
            OR a.version_modelo_ia = N'hybrid-stat-rules-v1.0'
      );

    DECLARE @ignoredCount INT = (SELECT COUNT(1) FROM @anomaliesToIgnore);

    IF @ignoredCount = 0
    BEGIN
        PRINT 'No se encontraron anomalías demo pendientes visibles para marcar como IGNORADA.';
        COMMIT TRANSACTION;
        RETURN;
    END;

    UPDATE a
    SET estado = 'IGNORADA',
        ticket_id = NULL,
        resuelta_el = NULL,
        resuelta_por = NULL,
        tecnico_responsable_id = NULL,
        tecnico_responsable_nombre = NULL
    FROM [energiaops].[anomalia] a
    INNER JOIN @anomaliesToIgnore d ON d.anomalia_id = a.anomalia_id
    WHERE a.inquilino_id = @tenantId;

    COMMIT TRANSACTION;

    PRINT CONCAT('Anomalías demo marcadas como IGNORADA: ', @ignoredCount);
    PRINT 'No se eliminaron lecturas, baselines, snapshots/KPIs ni datos de IA híbrida.';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;

    THROW;
END CATCH;
GO

/*
  Verificación sugerida:

  -- 1) Pantalla pendiente limpia para el medidor demo
  SELECT a.anomalia_id, a.estado, a.ticket_id, a.facility_label, a.meter_label, a.detectada_el
  FROM [energiaops].[anomalia] a
  WHERE a.inquilino_id = '11111111-1111-1111-1111-111111111111'
    AND a.medidor_id = (
        SELECT medidor_id
        FROM [core].[medidor]
        WHERE inquilino_id = '11111111-1111-1111-1111-111111111111'
          AND codigo_medidor = N'MED-DEMO-001'
    )
    AND a.estado = 'DETECTADA'
    AND a.ticket_id IS NULL;

  -- 2) Anomalías conservadas como trazabilidad
  SELECT COUNT(*) AS anomalias_ignoradas_demo
  FROM [energiaops].[anomalia] a
  WHERE a.inquilino_id = '11111111-1111-1111-1111-111111111111'
    AND a.estado = 'IGNORADA';

  -- 3) Lecturas demo intactas para IA híbrida
  SELECT COUNT(*) AS lecturas_demo_ia
  FROM [consumo].[lectura] l
  INNER JOIN [core].[medidor] m ON m.medidor_id = l.medidor_id
  WHERE l.inquilino_id = '11111111-1111-1111-1111-111111111111'
    AND m.codigo_medidor = N'MED-DEMO-001';

  -- 4) API/UI:
  -- GET /api/analytics/anomalies ya no devuelve IGNORADA/ARCHIVADA/DESCARTADA.
*/
