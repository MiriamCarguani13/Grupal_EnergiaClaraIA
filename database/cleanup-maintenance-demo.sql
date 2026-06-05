/*
  Limpieza segura SOLO para datos demo de mantenimiento/tickets.

  Conserva:
  - consumo.lectura / energy readings
  - energiaops.anomalia / energy anomalies historicas
  - energiaops.linea_base y datos usados por IA hibrida
  - analitica.kpi_* y snapshots
  - usuarios, roles, tenant, medidores, edificio y politica_sla demo

  Limpia:
  - tickets demo del tenant demo
  - ficha tecnica demo embebida en mantenimiento.ticket, al eliminar el ticket
  - evidencias/historial/checklist demo asociados a esos tickets, si existen
  - relacion energiaops.anomalia.ticket_id solo cuando apunta a tickets eliminados
*/

USE EnergiaClaraDB;
GO

SET NOCOUNT ON;
SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    DECLARE @tenantId UNIQUEIDENTIFIER = '11111111-1111-1111-1111-111111111111';
    DECLARE @adminId UNIQUEIDENTIFIER = '44444444-4444-4444-4444-444444444444';
    DECLARE @tecnicoId UNIQUEIDENTIFIER = '55555555-5555-5555-5555-555555555555';
    DECLARE @slaDemoId UNIQUEIDENTIFIER = '66666666-6666-6666-6666-666666666666';
    DECLARE @seedTicketId UNIQUEIDENTIFIER = '77777777-7777-7777-7777-777777777777';

    DECLARE @ticketsToDelete TABLE (
        ticket_id UNIQUEIDENTIFIER PRIMARY KEY
    );

    /*
      Filtros demo:
      - ticket fijo del seed tecnico/mantenimiento
      - tenant demo + SLA demo usado por el adapter actual
      - tenant demo + tecnico demo + titulos/descripciones generadas por pruebas MVP
      - tenant demo + evidencias guardadas por la UI demo en /uploads/ticket-evidence

      No se borra por tenant solamente.
    */
    INSERT INTO @ticketsToDelete (ticket_id)
    SELECT DISTINCT t.ticket_id
    FROM [mantenimiento].[ticket] t
    WHERE t.inquilino_id = @tenantId
      AND (
            t.ticket_id = @seedTicketId
            OR t.politica_sla_id = @slaDemoId
            OR (
                (t.asignado_a = @tecnicoId OR t.tecnico_responsable_id = @tecnicoId OR t.creado_por = @adminId)
                AND (
                    UPPER(t.titulo) LIKE N'%DEMO%'
                    OR UPPER(t.titulo) LIKE N'TICKET PRUEBA%'
                    OR UPPER(t.titulo) LIKE N'TICKET POR ANOMAL%'
                    OR UPPER(t.titulo) LIKE N'REVISION ELECTRICA AULA 3B%'
                    OR UPPER(ISNULL(t.descripcion, N'')) LIKE N'%VALIDACION%'
                    OR UPPER(ISNULL(t.descripcion, N'')) LIKE N'%TICKET GENERADO DESDE ANOMAL%'
                    OR ISNULL(t.evidencia_imagen_url, N'') LIKE N'/uploads/ticket-evidence/%'
                )
            )
      );

    DECLARE @ticketCount INT = (SELECT COUNT(1) FROM @ticketsToDelete);

    IF @ticketCount = 0
    BEGIN
        PRINT 'No se encontraron tickets demo de mantenimiento para limpiar.';
        COMMIT TRANSACTION;
        RETURN;
    END;

    /*
      Limpiar relacion directa anomalyId -> ticketId sin borrar anomalias.
      Solo toca anomalias vinculadas a tickets demo que se van a eliminar.
    */
    IF COL_LENGTH('energiaops.anomalia', 'ticket_id') IS NOT NULL
    BEGIN
        UPDATE a
        SET ticket_id = NULL,
            estado = CASE
                WHEN a.estado IN ('DERIVADA', 'EN_ATENCION', 'RESUELTA') THEN 'DETECTADA'
                ELSE a.estado
            END,
            resuelta_el = CASE WHEN COL_LENGTH('energiaops.anomalia', 'resuelta_el') IS NOT NULL THEN NULL ELSE resuelta_el END,
            resuelta_por = CASE WHEN COL_LENGTH('energiaops.anomalia', 'resuelta_por') IS NOT NULL THEN NULL ELSE resuelta_por END,
            tecnico_responsable_id = CASE WHEN COL_LENGTH('energiaops.anomalia', 'tecnico_responsable_id') IS NOT NULL THEN NULL ELSE tecnico_responsable_id END,
            tecnico_responsable_nombre = CASE WHEN COL_LENGTH('energiaops.anomalia', 'tecnico_responsable_nombre') IS NOT NULL THEN NULL ELSE tecnico_responsable_nombre END
        FROM [energiaops].[anomalia] a
        INNER JOIN @ticketsToDelete d ON d.ticket_id = a.ticket_id
        WHERE a.inquilino_id = @tenantId;
    END;

    /*
      analitica.resumen_impacto.ticket_id es nullable. Se conserva el resumen y solo
      se elimina la referencia al ticket demo para no tocar historiales energeticos.
    */
    IF OBJECT_ID('analitica.resumen_impacto', 'U') IS NOT NULL
       AND COL_LENGTH('analitica.resumen_impacto', 'ticket_id') IS NOT NULL
    BEGIN
        UPDATE ri
        SET ticket_id = NULL
        FROM [analitica].[resumen_impacto] ri
        INNER JOIN @ticketsToDelete d ON d.ticket_id = ri.ticket_id
        WHERE ri.inquilino_id = @tenantId;
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

    /*
      La ficha tecnica actual del MVP vive como columnas en mantenimiento.ticket:
      diagnostico_tecnico, solucion_aplicada, materiales_utilizados,
      observaciones_tecnicas, estado_tecnico, atendido_el, tecnico_responsable_id,
      evidencia_imagen_url. Al borrar el ticket demo, se elimina esa ficha demo.
    */
    DELETE t
    FROM [mantenimiento].[ticket] t
    INNER JOIN @ticketsToDelete d ON d.ticket_id = t.ticket_id
    WHERE t.inquilino_id = @tenantId;

    COMMIT TRANSACTION;

    PRINT CONCAT('Tickets demo eliminados: ', @ticketCount);
    PRINT 'Lecturas, anomalias historicas, baselines, KPIs/snapshots y datos de IA hibrida fueron conservados.';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;

    THROW;
END CATCH;
GO

/*
  Verificacion sugerida despues de ejecutar:

  -- 1) No quedan tickets demo del seed/MVP
  SELECT ticket_id, titulo, estado, anomalia_id, asignado_a, politica_sla_id
  FROM [mantenimiento].[ticket]
  WHERE inquilino_id = '11111111-1111-1111-1111-111111111111'
    AND (
        ticket_id = '77777777-7777-7777-7777-777777777777'
        OR politica_sla_id = '66666666-6666-6666-6666-666666666666'
        OR UPPER(titulo) LIKE N'%DEMO%'
        OR UPPER(titulo) LIKE N'TICKET PRUEBA%'
        OR UPPER(titulo) LIKE N'TICKET POR ANOMAL%'
    );

  -- 2) No quedan anomalias apuntando a tickets inexistentes
  SELECT a.anomalia_id, a.estado, a.ticket_id
  FROM [energiaops].[anomalia] a
  LEFT JOIN [mantenimiento].[ticket] t ON t.ticket_id = a.ticket_id
  WHERE a.inquilino_id = '11111111-1111-1111-1111-111111111111'
    AND a.ticket_id IS NOT NULL
    AND t.ticket_id IS NULL;

  -- 3) Historial IA intacto
  SELECT COUNT(*) AS lecturas_demo
  FROM [consumo].[lectura] l
  INNER JOIN [core].[medidor] m ON m.medidor_id = l.medidor_id
  WHERE l.inquilino_id = '11111111-1111-1111-1111-111111111111'
    AND m.codigo_medidor = N'MED-DEMO-001';

  SELECT COUNT(*) AS anomalias_demo
  FROM [energiaops].[anomalia]
  WHERE inquilino_id = '11111111-1111-1111-1111-111111111111';
*/
