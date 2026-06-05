/*
  Seed seguro de historial realista para probar IA hibrida explicable.

  Objetivo:
  - Insertar 10 lecturas normales para MED-DEMO-001.
  - No insertar la lectura anomala final; esa se prueba por API con 270 kWh.
  - Respetar uq_lectura_periodo usando periodo_inicio fijo y unico.
*/

SET NOCOUNT ON;
SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    DECLARE @tenantId UNIQUEIDENTIFIER = '11111111-1111-1111-1111-111111111111';
    DECLARE @adminId UNIQUEIDENTIFIER = '44444444-4444-4444-4444-444444444444';
    DECLARE @meterCode NVARCHAR(80) = N'MED-DEMO-001';
    DECLARE @facilityLabel NVARCHAR(80) = N'Sede Central - Bloque B - Aula 3B';
    DECLARE @medidorId UNIQUEIDENTIFIER;

    SELECT @medidorId = medidor_id
    FROM [core].[medidor]
    WHERE inquilino_id = @tenantId
      AND codigo_medidor = @meterCode;

    IF @medidorId IS NULL
    BEGIN
        THROW 51010, 'No existe el medidor demo MED-DEMO-001. Ejecuta database/seeds.sql antes del seed AI demo.', 1;
    END;

    IF NOT EXISTS (SELECT 1 FROM [iam].[usuario] WHERE usuario_id = @adminId AND inquilino_id = @tenantId)
    BEGIN
        THROW 51011, 'No existe el usuario admin demo esperado. Ejecuta database/seeds.sql antes del seed AI demo.', 1;
    END;

    DECLARE @readings TABLE (
        periodo_inicio DATETIME2(0) NOT NULL PRIMARY KEY,
        kwh DECIMAL(18,4) NOT NULL,
        voltaje DECIMAL(10,3) NOT NULL,
        factor_potencia DECIMAL(5,3) NOT NULL
    );

    INSERT INTO @readings (periodo_inicio, kwh, voltaje, factor_potencia)
    VALUES
        ('2026-06-03T08:00:00', 118.0000, 219.800, 0.950),
        ('2026-06-03T09:00:00', 122.0000, 220.100, 0.951),
        ('2026-06-03T10:00:00', 125.0000, 219.900, 0.949),
        ('2026-06-03T11:00:00', 130.0000, 220.300, 0.952),
        ('2026-06-03T12:00:00', 128.0000, 220.000, 0.950),
        ('2026-06-03T13:00:00', 134.0000, 220.400, 0.948),
        ('2026-06-03T14:00:00', 137.0000, 220.200, 0.951),
        ('2026-06-03T15:00:00', 140.0000, 219.700, 0.949),
        ('2026-06-03T16:00:00', 136.0000, 220.000, 0.950),
        ('2026-06-03T17:00:00', 142.0000, 220.500, 0.952);

    INSERT INTO [consumo].[lectura]
        (lectura_id, inquilino_id, medidor_id, valor, unidad, fecha_lectura,
         periodo_inicio, periodo_fin, origen, estado, registrada_por, creado_en,
         facility_label, meter_label, voltaje, factor_potencia)
    SELECT
        NEWID(),
        @tenantId,
        @medidorId,
        r.kwh,
        N'kWh',
        CAST(r.periodo_inicio AS DATE),
        r.periodo_inicio,
        DATEADD(HOUR, 1, r.periodo_inicio),
        N'API',
        N'VALIDADA',
        @adminId,
        SYSUTCDATETIME(),
        @facilityLabel,
        @meterCode,
        r.voltaje,
        r.factor_potencia
    FROM @readings r
    WHERE NOT EXISTS (
        SELECT 1
        FROM [consumo].[lectura] l
        WHERE l.medidor_id = @medidorId
          AND l.periodo_inicio = r.periodo_inicio
    );

    COMMIT TRANSACTION;

    PRINT 'Seed EnergyOps AI demo completado: historial normal insertado para MED-DEMO-001.';
    PRINT 'Siguiente prueba por API: enviar kWh=270 con measuredAt=2026-06-03T19:00:00Z.';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;

    THROW;
END CATCH;

