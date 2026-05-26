USE EnergiaClaraDB;
GO

IF NOT EXISTS (SELECT * FROM sys.schemas WHERE name = 'mantenimiento')
BEGIN
    EXEC('CREATE SCHEMA [mantenimiento]');
END
GO

IF NOT EXISTS (SELECT * FROM sys.tables WHERE object_id = OBJECT_ID(N'[mantenimiento].[ticket]'))
BEGIN
    CREATE TABLE [mantenimiento].[ticket] (
        ticket_id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY,
        inquilino_id UNIQUEIDENTIFIER NOT NULL,
        edificio_id UNIQUEIDENTIFIER NOT NULL,
        anomalia_id UNIQUEIDENTIFIER NULL,
        politica_sla_id UNIQUEIDENTIFIER NOT NULL,
        titulo NVARCHAR(300) NOT NULL,
        descripcion NVARCHAR(MAX) NULL,
        prioridad NVARCHAR(10) NOT NULL,
        estado NVARCHAR(20) NOT NULL,
        asignado_a UNIQUEIDENTIFIER NULL,
        creado_por UNIQUEIDENTIFIER NOT NULL,
        vencimiento_sla DATETIME2 NOT NULL,
        sla_incumplido BIT NOT NULL,
        cerrado_el DATETIME2 NULL,
        cerrado_por UNIQUEIDENTIFIER NULL,
        conteo_reapertura INT NOT NULL,
        creado_en DATETIME2 NOT NULL,
        actualizado_en DATETIME2 NOT NULL,
        CONSTRAINT FK_Ticket_Inquilino FOREIGN KEY (inquilino_id) REFERENCES [core].[inquilino](inquilino_id),
        CONSTRAINT FK_Ticket_Edificio FOREIGN KEY (edificio_id) REFERENCES [core].[edificio](edificio_id),
        CONSTRAINT FK_Ticket_Anomalia FOREIGN KEY (anomalia_id) REFERENCES [energiaops].[anomalia](anomalia_id),
        CONSTRAINT FK_Ticket_AsignadoA FOREIGN KEY (asignado_a) REFERENCES [iam].[usuario](usuario_id),
        CONSTRAINT FK_Ticket_CreadoPor FOREIGN KEY (creado_por) REFERENCES [iam].[usuario](usuario_id)
    );
END
GO
