/*
  Limpieza segura de datos demo para IA hibrida explicable.

  Alcance:
  - Solo datos EnergyOps/Analytics asociados al tenant demo y medidor MED-DEMO-001.
  - Conserva IAM, tenant, edificio, medidor y snapshot_linea_base.
  - Conserva anomalias que ya tengan tickets asociados para no romper FKs de mantenimiento.
*/

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
        THROW 51000, 'No existe el medidor demo MED-DEMO-001 para el tenant esperado. Ejecuta database/seeds.sql antes de esta limpieza.', 1;
    END;

    DECLARE @readingsToDelete TABLE (lectura_id UNIQUEIDENTIFIER PRIMARY KEY);
    DECLARE @anomaliesToDelete TABLE (anomalia_id UNIQUEIDENTIFIER PRIMARY KEY);
    DECLARE @auditEventsToDelete TABLE (evento_id UNIQUEIDENTIFIER PRIMARY KEY);

    INSERT INTO @readingsToDelete (lectura_id)
    SELECT lectura_id
    FROM [consumo].[lectura]
    WHERE inquilino_id = @tenantId
      AND medidor_id = @medidorId;

    INSERT INTO @anomaliesToDelete (anomalia_id)
    SELECT a.anomalia_id
    FROM [energiaops].[anomalia] a
    WHERE a.inquilino_id = @tenantId
      AND (
            a.medidor_id = @medidorId
            OR EXISTS (
                SELECT 1
                FROM @readingsToDelete r
                WHERE r.lectura_id = a.lectura_id
            )
      )
      AND NOT EXISTS (
            SELECT 1
            FROM [mantenimiento].[ticket] t
            WHERE t.anomalia_id = a.anomalia_id
      );

    INSERT INTO @auditEventsToDelete (evento_id)
    SELECT e.evento_id
    FROM [audit].[evento_auditoria] e
    WHERE e.inquilino_id = @tenantId
      AND (
            e.endpoint LIKE N'%/api/energyops/%'
            OR e.tipo_recurso IN (N'EnergyReading', N'EnergyAnomaly', N'LecturaEnergetica', N'AnomaliaEnergetica')
            OR EXISTS (
                SELECT 1
                FROM @readingsToDelete r
                WHERE CONVERT(NVARCHAR(50), r.lectura_id) = e.recurso_id
            )
            OR EXISTS (
                SELECT 1
                FROM @anomaliesToDelete a
                WHERE CONVERT(NVARCHAR(50), a.anomalia_id) = e.recurso_id
            )
      );

    DELETE lc
    FROM [audit].[log_cambios] lc
    INNER JOIN @auditEventsToDelete e ON e.evento_id = lc.evento_id;

    DELETE e
    FROM [audit].[evento_auditoria] e
    INNER JOIN @auditEventsToDelete d ON d.evento_id = e.evento_id;

    DELETE ri
    FROM [analitica].[resumen_impacto] ri
    INNER JOIN @anomaliesToDelete a ON a.anomalia_id = ri.anomalia_id;

    DELETE kd
    FROM [analitica].[kpi_diario] kd
    WHERE kd.inquilino_id = @tenantId
      AND kd.medidor_id = @medidorId;

    DELETE lp
    FROM [energiaops].[log_prediccion_ia] lp
    WHERE lp.inquilino_id = @tenantId
      AND lp.medidor_id = @medidorId;

    DELETE a
    FROM [energiaops].[anomalia] a
    INNER JOIN @anomaliesToDelete d ON d.anomalia_id = a.anomalia_id;

    DELETE l
    FROM [consumo].[lectura] l
    INNER JOIN @readingsToDelete d ON d.lectura_id = l.lectura_id
    WHERE NOT EXISTS (
        SELECT 1
        FROM [energiaops].[anomalia] a
        WHERE a.lectura_id = l.lectura_id
    );

    COMMIT TRANSACTION;

    PRINT 'Cleanup EnergyOps AI demo completado. Se conservaron IAM, tenant, medidor y baseline/configuracion.';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;

    THROW;
END CATCH;

