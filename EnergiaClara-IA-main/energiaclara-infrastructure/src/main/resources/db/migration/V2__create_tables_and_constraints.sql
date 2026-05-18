USE EnergiaClaraDB
;
/****** Objeto: Schema analitica Fecha de script: 12/05/2026 09:54:33 p. m. ******/
CREATE SCHEMA analitica
;
/****** Objeto: Schema audit Fecha de script: 12/05/2026 09:54:33 p. m. ******/
CREATE SCHEMA audit
;
/****** Objeto: Schema consumo Fecha de script: 12/05/2026 09:54:33 p. m. ******/
CREATE SCHEMA consumo
;
/****** Objeto: Schema core Fecha de script: 12/05/2026 09:54:33 p. m. ******/
CREATE SCHEMA core
;
/****** Objeto: Schema educacion Fecha de script: 12/05/2026 09:54:33 p. m. ******/
CREATE SCHEMA educacion
;
/****** Objeto: Schema energiaops Fecha de script: 12/05/2026 09:54:33 p. m. ******/
CREATE SCHEMA energiaops
;
/****** Objeto: Schema iam Fecha de script: 12/05/2026 09:54:33 p. m. ******/
CREATE SCHEMA iam
;
/****** Objeto: Schema mantenimiento Fecha de script: 12/05/2026 09:54:33 p. m. ******/
CREATE SCHEMA mantenimiento
;
/****** Objeto: Table analitica.kpi_diario Fecha de script: 12/05/2026 09:54:33 p. m. ******/

CREATE TABLE analitica.kpi_diario(
                                     kpi_id UUID NOT NULL,
                                     inquilino_id UUID NOT NULL,
                                     edificio_id UUID NULL,
                                     medidor_id UUID NULL,
                                     fecha_kpi date NOT NULL,
                                     total_kwh decimal(18, 4) NOT NULL,
                                     linea_base_kwh decimal(18, 4) NULL,
                                     kwh_ahorrados decimal(18, 4) NULL,
                                     co2_ton_evitadas decimal(18, 6) NULL,
                                     costo_evitado decimal(18, 4) NULL,
                                     conteo_anomalias int NOT NULL,
                                     tickets_abiertos int NOT NULL,
                                     calculado_el datetime2(3) NOT NULL,
                                     CONSTRAINT pk_kpi_diario PRIMARY KEY CLUSTERED
                                         (
                                         kpi_id ASC
                                         )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON PRIMARY,
                                     CONSTRAINT uq_kpi_diario_alcance] UNIQUE NONCLUSTERED
(
    inquilino_id ASC,
    edificio_id ASC,
    medidor_id ASC,
    fecha_kpi ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON PRIMARY
) ON PRIMARY;
/****** Objeto: Table analitica.kpi_mensual Fecha de script: 12/05/2026 09:54:33 p. m. ******/
CREATE TABLE analitica.kpi_mensual(
                                      kpi_id UUID NOT NULL,
                                      inquilino_id UUID NOT NULL,
                                      edificio_id UUID NULL,
                                      anio_kpi smallint NOT NULL,
                                      mes_kpi tinyint NOT NULL,
                                      total_kwh decimal(18, 4) NOT NULL,
                                      kwh_ahorrados decimal(18, 4) NULL,
                                      co2_ton_evitadas decimal(18, 6) NULL,
                                      costo_evitado decimal(18, 4) NULL,
                                      medidores_activos int NOT NULL,
                                      tickets_cerrados int NOT NULL,
                                      cumplimiento_sla decimal(5, 2) NULL,
                                      calculado_el datetime2(3) NOT NULL,
                                      CONSTRAINT pk_kpi_mensual PRIMARY KEY CLUSTERED
                                          (
                                          kpi_id ASC
                                          )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON PRIMARY
) ON PRIMARY;
/****** Objeto: Table analitica.resumen_impacto Fecha de script: 12/05/2026 09:54:33 p. m. ******/
CREATE TABLE analitica.resumen_impacto(
                                          resumen_id UUID NOT NULL,
                                          inquilino_id UUID NOT NULL,
                                          anomalia_id UUID NULL,
                                          ticket_id UUID NULL,
                                          kwh_estimados decimal(18, 4) NULL,
                                          kwh_reales decimal(18, 4) NULL,
                                          costo_estimado decimal(18, 4) NULL,
                                          costo_real decimal(18, 4) NULL,
                                          co2_estimado decimal(18, 6) NULL,
                                          co2_actual decimal(18, 6) NULL,
                                          calculado_el datetime2(3) NOT NULL,
                                          CONSTRAINT pk_resumen_impacto PRIMARY KEY CLUSTERED
                                              (
                                              resumen_id ASC
                                              )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON PRIMARY
) ON PRIMARY;
/****** Objeto: Table audit.evento_auditoria Fecha de script: 12/05/2026 09:54:33 p. m. ******/
CREATE TABLE audit.evento_auditoria(
                                       evento_id UUID NOT NULL,
                                       inquilino_id UUID NOT NULL,
                                       actor_id UUID NOT NULL,
                                       accion nvarchar(80) NOT NULL,
                                       tipo_recurso nvarchar(50) NOT NULL,
                                       recurso_id nvarchar(50) NOT NULL,
                                       hash_anterior nvarchar(64) NULL,
                                       hash_posterior nvarchar(64) NULL,
                                       direccion_ip nvarchar(45) NULL,
                                       id_correlacion UUID NULL,
                                       severidad nvarchar(10) NOT NULL,
                                       ocurrido_el datetime2(3) NOT NULL,
                                       CONSTRAINT pk_evento_auditoria PRIMARY KEY CLUSTERED
                                           (
                                           evento_id ASC
                                           )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON PRIMARY
) ON PRIMARY;
/****** Objeto: Table audit.log_cambios Fecha de script: 12/05/2026 09:54:33 p. m. ******/
CREATE TABLE audit.log_cambios(
                                  cambio_id UUID NOT NULL,
                                  evento_id UUID NOT NULL,
                                  inquilino_id UUID NOT NULL,
                                  nombre_campo nvarchar(100) NOT NULL,
                                  valor_anterior nvarchar(max) NULL,
                                  valor_nuevo nvarchar(max) NULL,
                                  CONSTRAINT pk_log_cambios PRIMARY KEY CLUSTERED
                                      (
                                      cambio_id ASC
                                      )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON PRIMARY
) ON PRIMARY TEXTIMAGE_ON PRIMARY;
/****** Objeto: Table consumo.importacion_lectura Fecha de script: 12/05/2026 09:54:33 p. m. ******/
CREATE TABLE consumo.importacion_lectura(
                                            importacion_id UUID NOT NULL,
                                            inquilino_id UUID NOT NULL,
                                            nombre_archivo nvarchar(300) NOT NULL,
                                            hash_archivo nvarchar(64) NOT NULL,
                                            total_filas int NOT NULL,
                                            filas_procesadas int NOT NULL,
                                            filas_error int NOT NULL,
                                            estado nvarchar(20) NOT NULL,
                                            importado_por UUID NOT NULL,
                                            iniciado_el datetime2(3) NOT NULL,
                                            finalizado_el datetime2(3) NULL,
                                            detalle_error nvarchar(max) NULL,
                                            CONSTRAINT pk_importacion_lectura PRIMARY KEY CLUSTERED
                                                (
                                                importacion_id ASC
                                                )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON PRIMARY
) ON PRIMARY TEXTIMAGE_ON PRIMARY;
/****** Objeto: Table consumo.lectura Fecha de script: 12/05/2026 09:54:33 p. m. ******/
CREATE TABLE consumo.lectura(
                                lectura_id UUID NOT NULL,
                                inquilino_id UUID NOT NULL,
                                medidor_id UUID NOT NULL,
                                valor decimal(18, 4) NOT NULL,
                                unidad nvarchar(10) NOT NULL,
                                fecha_lectura date NOT NULL,
                                periodo_inicio datetime2(0) NOT NULL,
                                periodo_fin datetime2(0) NOT NULL,
                                origen nvarchar(20) NOT NULL,
                                estado nvarchar(20) NOT NULL,
                                registrada_por UUID NOT NULL,
                                validada_por UUID NULL,
                                validada_el datetime2(3) NULL,
                                anulada_por UUID NULL,
                                anulada_el datetime2(3) NULL,
                                motivo_anulacion nvarchar(500) NULL,
                                id_correlacion UUID NULL,
                                version_fila timestamp NOT NULL,
                                creado_en datetime2(3) NOT NULL,
                                CONSTRAINT pk_lectura PRIMARY KEY CLUSTERED
                                    (
                                    lectura_id ASC
                                    )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON PRIMARY,
                                CONSTRAINT uq_lectura_periodo UNIQUE NONCLUSTERED
                                    (
                                    medidor_id ASC,
                                    periodo_inicio ASC
                                    )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON PRIMARY
) ON PRIMARY;
/****** Objeto: Table core.edificio Fecha de script: 12/05/2026 09:54:33 p. m. ******/
CREATE TABLE core.edificio(
                              edificio_id UUID NOT NULL,
                              inquilino_id UUID NOT NULL,
                              nombre nvarchar(200) NOT NULL,
                              direccion nvarchar(500) NULL,
                              ciudad nvarchar(100) NULL,
                              tipo_edificio nvarchar(50) NULL,
                              latitud decimal(9, 6) NULL,
                              longitud decimal(9, 6) NULL,
                              esta_activo bit NOT NULL,
                              creado_en datetime2(3) NOT NULL,
                              CONSTRAINT pk_edificio PRIMARY KEY CLUSTERED
                                  (
                                  edificio_id ASC
                                  )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON PRIMARY
) ON PRIMARY;
/****** Objeto: Table core.equipo Fecha de script: 12/05/2026 09:54:33 p. m. ******/
CREATE TABLE core.equipo(
                            equipo_id UUID NOT NULL,
                            inquilino_id UUID NOT NULL,
                            edificio_id UUID NOT NULL,
                            medidor_id UUID NULL,
                            nombre nvarchar(200) NOT NULL,
                            categoria nvarchar(50) NULL,
                            marca nvarchar(100) NULL,
                            modelo nvarchar(100) NULL,
                            numero_serie nvarchar(100) NULL,
                            potencia_kw decimal(10, 3) NULL,
                            esta_activo bit NOT NULL,
                            instalado_el date NULL,
                            creado_en datetime2(3) NOT NULL,
                            CONSTRAINT pk_equipo PRIMARY KEY CLUSTERED
                                (
                                equipo_id ASC
                                )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON PRIMARY
) ON PRIMARY;
/****** Objeto: Table core.inquilino Fecha de script: 12/05/2026 09:54:33 p. m. ******/
CREATE TABLE core.inquilino(
                               inquilino_id UUID NOT NULL,
                               nombre nvarchar(200) NOT NULL,
                               nombre_legal nvarchar(300) NULL,
                               nit_rut nvarchar(50) NULL,
                               tipo_plan nvarchar(20) NOT NULL,
                               factor_co2 decimal(10, 6) NOT NULL,
                               codigo_moneda char(3) NOT NULL,
                               esta_activo bit NOT NULL,
                               creado_en datetime2(3) NOT NULL,
                               actualizado_en datetime2(3) NOT NULL,
                               CONSTRAINT pk_inquilino PRIMARY KEY CLUSTERED
                                   (
                                   inquilino_id ASC
                                   )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON PRIMARY
) ON PRIMARY;
/****** Objeto: Table core.medidor Fecha de script: 12/05/2026 09:54:33 p. m. ******/
CREATE TABLE core.medidor(
                             medidor_id UUID NOT NULL,
                             inquilino_id UUID NOT NULL,
                             edificio_id UUID NOT NULL,
                             codigo_medidor nvarchar(50) NOT NULL,
                             tipo_medidor nvarchar(30) NOT NULL,
                             unidad nvarchar(10) NOT NULL,
                             descripcion_ubicacion nvarchar(200) NULL,
                             esta_activo bit NOT NULL,
                             instalado_el date NULL,
                             creado_en datetime2(3) NOT NULL,
                             actualizado_en datetime2(3) NOT NULL,
                             CONSTRAINT pk_medidor PRIMARY KEY CLUSTERED
                                 (
                                 medidor_id ASC
                                 )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON PRIMARY,
                             CONSTRAINT uq_codigo_medidor_inquilino UNIQUE NONCLUSTERED
                                 (
                                 inquilino_id ASC,
                                 codigo_medidor ASC
                                 )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON PRIMARY
) ON PRIMARY;
/****** Objeto: Table core.tarifa Fecha de script: 12/05/2026 09:54:33 p. m. ******/
CREATE TABLE core.tarifa(
                            tarifa_id UUID NOT NULL,
                            inquilino_id UUID NOT NULL,
                            nombre nvarchar(100) NOT NULL,
                            precio_por_kwh decimal(12, 6) NOT NULL,
                            valido_desde date NOT NULL,
                            valido_hasta date NULL,
                            creado_por UUID NOT NULL,
                            creado_en datetime2(3) NOT NULL,
                            CONSTRAINT pk_tarifa PRIMARY KEY CLUSTERED
                                (
                                tarifa_id ASC
                                )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON PRIMARY
) ON PRIMARY;
/****** Objeto: Table educacion.medalla Fecha de script: 12/05/2026 09:54:33 p. m. ******/
CREATE TABLE educacion.medalla(
                                  medalla_id UUID NOT NULL,
                                  codigo nvarchar(50) NOT NULL,
                                  nombre nvarchar(100) NOT NULL,
                                  descripcion nvarchar(300) NULL,
                                  url_icono nvarchar(500) NULL,
                                  puntos int NOT NULL,
                                  CONSTRAINT pk_medalla PRIMARY KEY CLUSTERED
                                      (
                                      medalla_id ASC
                                      )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON PRIMARY,
                                  CONSTRAINT uq_medalla_codigo UNIQUE NONCLUSTERED
                                      (
                                      medalla_id ASC
                                      )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON PRIMARY
) ON PRIMARY;
/****** Objeto: Table educacion.medalla_usuario Fecha de script: 12/05/2026 09:54:33 p. m. ******/
CREATE TABLE educacion.medalla_usuario(
                                          medalla_usuario_id UUID NOT NULL,
                                          usuario_id UUID NOT NULL,
                                          medalla_id UUID NOT NULL,
                                          inquilino_id UUID NOT NULL,
                                          reto_id UUID NULL,
                                          otorgada_el datetime2(3) NOT NULL,
                                          CONSTRAINT pk_medalla_usuario PRIMARY KEY CLUSTERED
                                              (
                                              medalla_usuario_id ASC
                                              )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON PRIMARY
) ON PRIMARY;
/****** Objeto: Table educacion.progreso_reto Fecha de script: 12/05/2026 09:54:33 p. m. ******/
CREATE TABLE educacion.progreso_reto(
                                        progreso_id UUID NOT NULL,
                                        reto_id UUID NOT NULL,
                                        inquilino_id UUID NOT NULL,
                                        usuario_id UUID NOT NULL,
                                        kwh_ahorrados decimal(18, 4) NOT NULL,
                                        conteo_acciones int NOT NULL,
                                        ultima_accion_el datetime2(3) NULL,
                                        registrado_el datetime2(3) NOT NULL,
                                        CONSTRAINT pk_progreso_reto PRIMARY KEY CLUSTERED
                                            (
                                            progreso_id ASC
                                            )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON PRIMARY
) ON PRIMARY;
/****** Objeto: Table educacion.reto Fecha de script: 12/05/2026 09:54:33 p. m. ******/
CREATE TABLE educacion.reto(
                               reto_id UUID NOT NULL,
                               inquilino_id UUID NOT NULL,
                               edificio_id UUID NOT NULL,
                               creado_por UUID NOT NULL,
                               titulo nvarchar(200) NOT NULL,
                               descripcion nvarchar(max) NULL,
                               meta_kwh decimal(18, 4) NOT NULL,
                               periodo_inicio date NOT NULL,
                               periodo_fin date NOT NULL,
                               estado nvarchar(20) NOT NULL,
                               version int NOT NULL,
                               esta_publicado bit NOT NULL,
                               publicado_el datetime2(3) NULL,
                               creado_en datetime2(3) NOT NULL,
                               actualizado_en datetime2(3) NOT NULL,
                               CONSTRAINT pk_reto PRIMARY KEY CLUSTERED
                                   (
                                   reto_id ASC
                                   )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON PRIMARY
) ON PRIMARY TEXTIMAGE_ON PRIMARY;
/****** Objeto: Table educacion.snapshot_ranking Fecha de script: 12/05/2026 09:54:33 p. m. ******/
CREATE TABLE educacion.snapshot_ranking(
                                           snapshot_id UUID NOT NULL,
                                           reto_id UUID NOT NULL,
                                           inquilino_id UUID NOT NULL,
                                           usuario_id UUID NOT NULL,
                                           posicion_rank int NOT NULL,
                                           total_kwh_ahorrados decimal(18, 4) NOT NULL,
                                           conteo_medallas int NOT NULL,
                                           capturado_el datetime2(3) NOT NULL,
                                           CONSTRAINT pk_snapshot_ranking PRIMARY KEY CLUSTERED
                                               (
                                               snapshot_id ASC
                                               )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON PRIMARY
) ON PRIMARY;
/****** Objeto: Table energiaops.anomalia Fecha de script: 12/05/2026 09:54:33 p. m. ******/
CREATE TABLE energiaops.anomalia(
                                    anomalia_id UUID NOT NULL,
                                    inquilino_id UUID NOT NULL,
                                    medidor_id UUID NOT NULL,
                                    lectura_id UUID NULL,
                                    tipo_anomalia nvarchar(30) NOT NULL,
                                    severidad nvarchar(10) NOT NULL,
                                    puntaje_score decimal(5, 4) NOT NULL,
                                    porcentaje_desviacion decimal(10, 4) NULL,
                                    explicacion nvarchar(max) NOT NULL,
                                    ia_utilizada bit NOT NULL,
                                    version_modelo_ia nvarchar(50) NULL,
                                    estado nvarchar(20) NOT NULL,
                                    detectada_el datetime2(3) NOT NULL,
                                    resuelta_el datetime2(3) NULL,
                                    resuelta_por UUID NULL,
                                    version_fila timestamp NOT NULL,
                                    CONSTRAINT pk_anomalia PRIMARY KEY CLUSTERED
                                        (
                                        anomalia_id ASC
                                        )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON PRIMARY
) ON PRIMARY TEXTIMAGE_ON PRIMARY;
/****** Objeto: Table energiaops.log_prediccion_ia Fecha de script: 12/05/2026 09:54:33 p. m. ******/
CREATE TABLE energiaops.log_prediccion_ia(
                                             log_id UUID NOT NULL,
                                             inquilino_id UUID NOT NULL,
                                             medidor_id UUID NOT NULL,
                                             version_modelo nvarchar(50) NOT NULL,
                                             hash_entrada nvarchar(64) NOT NULL,
                                             hash_salida nvarchar(64) NOT NULL,
                                             valor_predicho decimal(18, 4) NULL,
                                             confianza decimal(5, 4) NULL,
                                             anomalia_detectada bit NOT NULL,
                                             latencia_ms int NULL,
                                             llamada_el datetime2(3) NOT NULL,
                                             CONSTRAINT pk_log_prediccion_ia PRIMARY KEY CLUSTERED
                                                 (
                                                 log_id ASC
                                                 )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON PRIMARY
) ON PRIMARY;
/****** Objeto: Table energiaops.snapshot_linea_base Fecha de script: 12/05/2026 09:54:33 p. m. ******/
CREATE TABLE energiaops.snapshot_linea_base(
                                               linea_base_id UUID NOT NULL,
                                               inquilino_id UUID NOT NULL,
                                               medidor_id UUID NOT NULL,
                                               tipo_periodo nvarchar(10) NOT NULL,
                                               referencia_inicio date NOT NULL,
                                               referencia_fin date NOT NULL,
                                               valor_promedio decimal(18, 4) NOT NULL,
                                               valor_p95 decimal(18, 4) NOT NULL,
                                               desviacion_estandar decimal(18, 4) NULL,
                                               conteo_muestras int NOT NULL,
                                               calculado_el datetime2(3) NOT NULL,
                                               tolerancia_porcentaje decimal(5, 2) NULL,
                                               activo bit NOT NULL,
                                               facility_label nvarchar(150) NULL,
                                               meter_label nvarchar(150) NULL,
                                               CONSTRAINT pk_snapshot_linea_base PRIMARY KEY CLUSTERED
                                                   (
                                                   linea_base_id ASC
                                                   )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON PRIMARY
) ON PRIMARY;
/****** Objeto: Table iam.permiso Fecha de script: 12/05/2026 09:54:33 p. m. ******/
CREATE TABLE iam.permiso(
                            permiso_id UUID NOT NULL,
                            codigo nvarchar(100) NOT NULL,
                            descripcion nvarchar(300) NULL,
                            CONSTRAINT pk_permiso PRIMARY KEY CLUSTERED
                                (
                                permiso_id ASC
                                )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON PRIMARY,
                            CONSTRAINT uq_permiso_codigo UNIQUE NONCLUSTERED
                                (
                                codigo ASC
                                )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON PRIMARY
) ON PRIMARY;
/****** Objeto: Table iam.politica_abac Fecha de script: 12/05/2026 09:54:33 p. m. ******/
CREATE TABLE iam.politica_abac(
                                  politica_id UUID NOT NULL,
                                  rol_id UUID NOT NULL,
                                  tipo_recurso nvarchar(50) NOT NULL,
                                  accion nvarchar(50) NOT NULL,
                                  condicion_json nvarchar(max) NOT NULL,
                                  esta_activa bit NOT NULL,
                                  creado_en datetime2(3) NOT NULL,
                                  CONSTRAINT pk_politica_abac PRIMARY KEY CLUSTERED
                                      (
                                      politica_id ASC
                                      )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON PRIMARY
) ON PRIMARY TEXTIMAGE_ON PRIMARY;
/****** Objeto: Table iam.rol Fecha de script: 12/05/2026 09:54:33 p. m. ******/
CREATE TABLE iam.rol(
                        rol_id UUID NOT NULL,
                        nombre nvarchar(50) NOT NULL,
                        descripcion nvarchar(300) NULL,
                        nivel_alcance nvarchar(30) NOT NULL,
                        CONSTRAINT pk_rol PRIMARY KEY CLUSTERED
                            (
                            rol_id ASC
                            )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON PRIMARY,
                        CONSTRAINT uq_rol_nombre UNIQUE NONCLUSTERED
                            (
                            nombre ASC
                            )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON PRIMARY
) ON PRIMARY;
/****** Objeto: Table iam.rol_permiso Fecha de script: 12/05/2026 09:54:33 p. m. ******/
CREATE TABLE iam.rol_permiso(
                                rol_id UUID NOT NULL,
                                permiso_id UUID NOT NULL,
                                CONSTRAINT pk_rol_permiso PRIMARY KEY CLUSTERED
                                    (
                                    rol_id ASC,
                                    permiso_id ASC
                                    )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON PRIMARY
) ON PRIMARY;
/****** Objeto: Table iam.token_actualizacion Fecha de script: 12/05/2026 09:54:33 p. m. ******/
CREATE TABLE iam.token_actualizacion(
                                        token_id UUID NOT NULL,
                                        usuario_id UUID NOT NULL,
                                        token_hash nvarchar(500) NOT NULL,
                                        emitido_el datetime2(3) NOT NULL,
                                        expira_el datetime2(3) NOT NULL,
                                        revocado bit NOT NULL,
                                        CONSTRAINT pk_token_actualizacion PRIMARY KEY CLUSTERED
                                            (
                                            token_id ASC
                                            )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON PRIMARY
) ON PRIMARY;
/****** Objeto: Table iam.usuario Fecha de script: 12/05/2026 09:54:33 p. m. ******/
CREATE TABLE iam.usuario(
                            usuario_id UUID NOT NULL,
                            inquilino_id UUID NOT NULL,
                            correo nvarchar(200) NOT NULL,
                            nombre_completo nvarchar(200) NOT NULL,
                            contrasena_hash nvarchar(500) NOT NULL,
                            esta_activo bit NOT NULL,
                            ultimo_ingreso_el datetime2(3) NULL,
                            creado_en datetime2(3) NOT NULL,
                            actualizado_en datetime2(3) NOT NULL,
                            CONSTRAINT pk_usuario PRIMARY KEY CLUSTERED
                                (
                                usuario_id ASC
                                )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON PRIMARY,
                            CONSTRAINT uq_usuario_correo UNIQUE NONCLUSTERED
                                (
                                correo ASC
                                )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON PRIMARY
) ON PRIMARY;
/****** Objeto: Table iam.usuario_rol Fecha de script: 12/05/2026 09:54:33 p. m. ******/
CREATE TABLE iam.usuario_rol(
                                usuario_rol_id UUID NOT NULL,
                                usuario_id UUID NOT NULL,
                                rol_id UUID NOT NULL,
                                inquilino_id UUID NOT NULL,
                                edificio_id UUID NULL,
                                asignado_el datetime2(3) NOT NULL,
                                asignado_por UUID NOT NULL,
                                CONSTRAINT pk_usuario_rol PRIMARY KEY CLUSTERED
                                    (
                                    usuario_rol_id ASC
                                    )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON PRIMARY,
                                CONSTRAINT uq_usuario_rol_alcance UNIQUE NONCLUSTERED
                                    (
                                    usuario_id ASC,
                                    rol_id ASC,
                                    inquilino_id ASC,
                                    edificio_id ASC
                                    )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON PRIMARY
) ON PRIMARY;
/****** Objeto: Table mantenimiento.evidencia_ticket Fecha de script: 12/05/2026 09:54:33 p. m. ******/
CREATE TABLE mantenimiento.evidencia_ticket(
                                               evidencia_id UUID NOT NULL,
                                               ticket_id UUID NOT NULL,
                                               inquilino_id UUID NOT NULL,
                                               tipo_evidencia nvarchar(20) NOT NULL,
                                               url_archivo nvarchar(1000) NULL,
                                               hash_archivo nvarchar(64) NULL,
                                               notas nvarchar(max) NULL,
                                               subido_por UUID NOT NULL,
                                               subido_el datetime2(3) NOT NULL,
                                               CONSTRAINT pk_evidencia_ticket PRIMARY KEY CLUSTERED
                                                   (
                                                   evidencia_id ASC
                                                   )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON PRIMARY
) ON PRIMARY TEXTIMAGE_ON PRIMARY;
/****** Objeto: Table mantenimiento.historial_asignacion_ticket Fecha de script: 12/05/2026 09:54:33 p. m. ******/
CREATE TABLE mantenimiento.historial_asignacion_ticket(
                                                          historial_id UUID NOT NULL,
                                                          ticket_id UUID NOT NULL,
                                                          inquilino_id UUID NOT NULL,
                                                          asignado_a UUID NOT NULL,
                                                          asignado_por UUID NOT NULL,
                                                          motivo nvarchar(500) NULL,
                                                          asignado_el datetime2(3) NOT NULL,
                                                          CONSTRAINT pk_historial_asignacion PRIMARY KEY CLUSTERED
                                                              (
                                                              historial_id ASC
                                                              )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON PRIMARY
) ON PRIMARY;
/****** Objeto: Table mantenimiento.lista_verificacion_ticket Fecha de script: 12/05/2026 09:54:33 p. m. ******/
CREATE TABLE mantenimiento.lista_verificacion_ticket(
                                                        item_id UUID NOT NULL,
                                                        ticket_id UUID NOT NULL,
                                                        inquilino_id UUID NOT NULL,
                                                        descripcion nvarchar(300) NOT NULL,
                                                        esta_completado bit NOT NULL,
                                                        completado_por UUID NULL,
                                                        completado_el datetime2(3) NULL,
                                                        orden_visualizacion tinyint NOT NULL,
                                                        CONSTRAINT pk_lista_verificacion PRIMARY KEY CLUSTERED
                                                            (
                                                            item_id ASC
                                                            )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON PRIMARY
) ON PRIMARY;
/****** Objeto: Table mantenimiento.politica_sla Fecha de script: 12/05/2026 09:54:33 p. m. ******/
CREATE TABLE mantenimiento.politica_sla(
                                           politica_sla_id UUID NOT NULL,
                                           inquilino_id UUID NOT NULL,
                                           nombre nvarchar(100) NOT NULL,
                                           severidad nvarchar(10) NOT NULL,
                                           horas_respuesta int NOT NULL,
                                           horas_resolucion int NOT NULL,
                                           esta_activa bit NOT NULL,
                                           creado_en datetime2(3) NOT NULL,
                                           CONSTRAINT pk_politica_sla PRIMARY KEY CLUSTERED
                                               (
                                               politica_sla_id ASC
                                               )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON PRIMARY
) ON PRIMARY;
/****** Objeto: Table mantenimiento.ticket Fecha de script: 12/05/2026 09:54:33 p. m. ******/
CREATE TABLE mantenimiento.ticket(
                                     ticket_id UUID NOT NULL,
                                     inquilino_id UUID NOT NULL,
                                     edificio_id UUID NOT NULL,
                                     anomalia_id UUID NULL,
                                     politica_sla_id UUID NOT NULL,
                                     titulo nvarchar(300) NOT NULL,
                                     descripcion nvarchar(max) NULL,
                                     prioridad nvarchar(10) NOT NULL,
                                     estado nvarchar(20) NOT NULL,
                                     asignado_a UUID NULL,
                                     creado_por UUID NOT NULL,
                                     vencimiento_sla datetime2(3) NOT NULL,
                                     sla_incumplido bit NOT NULL,
                                     cerrado_el datetime2(3) NULL,
                                     cerrado_por UUID NULL,
                                     conteo_reapertura tinyint NOT NULL,
                                     version_fila timestamp NOT NULL,
                                     creado_en datetime2(3) NOT NULL,
                                     actualizado_en datetime2(3) NOT NULL,
                                     CONSTRAINT pk_ticket PRIMARY KEY CLUSTERED
                                         (
                                         ticket_id ASC
                                         )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON PRIMARY
) ON PRIMARY TEXTIMAGE_ON PRIMARY;
ALTER TABLE analitica.kpi_diario ADD DEFAULT (newsequentialid()) FOR kpi_id;
ALTER TABLE analitica.kpi_diario ADD  DEFAULT ((0)) FOR total_kwh;
ALTER TABLE analitica.kpi_diario ADD  DEFAULT ((0)) FOR conteo_anomalias;
ALTER TABLE analitica.kpi_diario ADD  DEFAULT ((0)) FOR tickets_abiertos;
ALTER TABLE analitica.kpi_diario ADD  DEFAULT (sysutcdatetime()) FOR calculado_el;
ALTER TABLE analitica.kpi_mensual ADD  DEFAULT (newsequentialid()) FOR kpi_id;
ALTER TABLE analitica.kpi_mensual ADD  DEFAULT ((0)) FOR total_kwh;
ALTER TABLE analitica.kpi_mensual ADD  DEFAULT ((0)) FOR medidores_activos;
ALTER TABLE analitica.kpi_mensual ADD  DEFAULT ((0)) FOR tickets_cerrados;
ALTER TABLE analitica.kpi_mensual ADD  DEFAULT (sysutcdatetime()) FOR calculado_el;
ALTER TABLE analitica.resumen_impacto ADD  DEFAULT (newsequentialid()) FOR resumen_id;
ALTER TABLE analitica.resumen_impacto ADD  DEFAULT (sysutcdatetime()) FOR calculado_el;
ALTER TABLE audit.evento_auditoria ADD  DEFAULT (newsequentialid()) FOR evento_id;
ALTER TABLE audit.evento_auditoria ADD  DEFAULT ('MEDIA') FOR severidad;
ALTER TABLE audit.evento_auditoria ADD  DEFAULT (sysutcdatetime()) FOR ocurrido_el;
ALTER TABLE audit.log_cambios ADD  DEFAULT (newsequentialid()) FOR cambio_id;
ALTER TABLE consumo.importacion_lectura ADD  DEFAULT (newsequentialid()) FOR importacion_id;
ALTER TABLE consumo.importacion_lectura ADD  DEFAULT ((0)) FOR total_filas;
ALTER TABLE consumo.importacion_lectura ADD  DEFAULT ((0)) FOR filas_procesadas;
ALTER TABLE consumo.importacion_lectura ADD  DEFAULT ((0)) FOR filas_error;
ALTER TABLE consumo.importacion_lectura ADD  DEFAULT ('PROCESANDO') FOR estado;
ALTER TABLE consumo.importacion_lectura ADD  DEFAULT (sysutcdatetime()) FOR iniciado_el;
ALTER TABLE consumo.lectura ADD  DEFAULT (newsequentialid()) FOR lectura_id;
ALTER TABLE consumo.lectura ADD  DEFAULT ('kWh') FOR unidad;
ALTER TABLE consumo.lectura ADD  DEFAULT ('MANUAL') FOR origen;
ALTER TABLE consumo.lectura ADD  DEFAULT ('PENDIENTE') FOR estado;
ALTER TABLE consumo.lectura ADD  DEFAULT (sysutcdatetime()) FOR creado_en;
ALTER TABLE core.edificio ADD  DEFAULT (newsequentialid()) FOR edificio_id;
ALTER TABLE core.edificio ADD  DEFAULT ((1)) FOR esta_activo;
ALTER TABLE core.edificio ADD  DEFAULT (sysutcdatetime()) FOR creado_en;
ALTER TABLE core.equipo ADD  DEFAULT (newsequentialid()) FOR equipo_id;
ALTER TABLE core.equipo ADD  DEFAULT ((1)) FOR esta_activo;
ALTER TABLE core.equipo ADD  DEFAULT (sysutcdatetime()) FOR creado_en;
ALTER TABLE core.inquilino ADD  DEFAULT (newsequentialid()) FOR inquilino_id;
ALTER TABLE core.inquilino ADD  DEFAULT ('BASIC') FOR tipo_plan;
ALTER TABLE core.inquilino ADD  DEFAULT ((0.000233)) FOR factor_co2;
ALTER TABLE core.inquilino ADD  DEFAULT ('BOB') FOR codigo_moneda;
ALTER TABLE core.inquilino ADD  DEFAULT ((1)) FOR esta_activo;
ALTER TABLE core.inquilino ADD  DEFAULT (sysutcdatetime()) FOR creado_en;
ALTER TABLE core.inquilino ADD  DEFAULT (sysutcdatetime()) FOR actualizado_en;
ALTER TABLE core.medidor ADD  DEFAULT (newsequentialid()) FOR medidor_id;
ALTER TABLE core.medidor ADD  DEFAULT ('kWh') FOR unidad;
ALTER TABLE core.medidor ADD  DEFAULT ((1)) FOR esta_activo;
ALTER TABLE core.medidor ADD  DEFAULT (sysutcdatetime()) FOR creado_en;
ALTER TABLE core.medidor ADD  DEFAULT (sysutcdatetime()) FOR actualizado_en;
ALTER TABLE core.tarifa ADD  DEFAULT (newsequentialid()) FOR tarifa_id;
ALTER TABLE core.tarifa ADD  DEFAULT (sysutcdatetime()) FOR creado_en;
ALTER TABLE educacion.medalla ADD  DEFAULT (newsequentialid()) FOR medalla_id;
ALTER TABLE educacion.medalla ADD  DEFAULT ((0)) FOR puntos;
ALTER TABLE educacion.medalla_usuario ADD  DEFAULT (newsequentialid()) FOR medalla_usuario_id;
ALTER TABLE educacion.medalla_usuario ADD  DEFAULT (sysutcdatetime()) FOR otorgada_el;
ALTER TABLE educacion.progreso_reto ADD  DEFAULT (newsequentialid()) FOR progreso_id;
ALTER TABLE educacion.progreso_reto ADD  DEFAULT ((0)) FOR kwh_ahorrados;
ALTER TABLE educacion.progreso_reto ADD  DEFAULT ((0)) FOR conteo_acciones;
ALTER TABLE educacion.progreso_reto ADD  DEFAULT (sysutcdatetime()) FOR registrado_el;
ALTER TABLE educacion.reto ADD  DEFAULT (newsequentialid()) FOR reto_id;
ALTER TABLE educacion.reto ADD  DEFAULT ('CREADO') FOR estado;
ALTER TABLE educacion.reto ADD  DEFAULT ((1)) FOR version;
ALTER TABLE educacion.reto ADD  DEFAULT ((0)) FOR esta_publicado;
ALTER TABLE educacion.reto ADD  DEFAULT (sysutcdatetime()) FOR creado_en;
ALTER TABLE educacion.reto ADD  DEFAULT (sysutcdatetime()) FOR actualizado_en;
ALTER TABLE educacion.snapshot_ranking ADD  DEFAULT (newsequentialid()) FOR snapshot_id;
ALTER TABLE educacion.snapshot_ranking ADD  DEFAULT ((0)) FOR conteo_medallas;
ALTER TABLE educacion.snapshot_ranking ADD  DEFAULT (sysutcdatetime()) FOR capturado_el;
ALTER TABLE energiaops.anomalia ADD  DEFAULT (newsequentialid()) FOR anomalia_id;
ALTER TABLE energiaops.anomalia ADD  DEFAULT ((0)) FOR ia_utilizada;
ALTER TABLE energiaops.anomalia ADD  DEFAULT ('DETECTADA') FOR estado;
ALTER TABLE energiaops.anomalia ADD  DEFAULT (sysutcdatetime()) FOR detectada_el;
ALTER TABLE energiaops.log_prediccion_ia ADD  DEFAULT (newsequentialid()) FOR log_id;
ALTER TABLE energiaops.log_prediccion_ia ADD  DEFAULT ((0)) FOR anomalia_detectada;
ALTER TABLE energiaops.log_prediccion_ia ADD  DEFAULT (sysutcdatetime()) FOR llamada_el;
ALTER TABLE energiaops.snapshot_linea_base ADD  DEFAULT (newsequentialid()) FOR linea_base_id;
ALTER TABLE energiaops.snapshot_linea_base ADD  DEFAULT ((0)) FOR conteo_muestras;
ALTER TABLE energiaops.snapshot_linea_base ADD  DEFAULT (sysutcdatetime()) FOR calculado_el;
ALTER TABLE iam.permiso ADD  DEFAULT (newsequentialid()) FOR permiso_id;
ALTER TABLE iam.politica_abac ADD  DEFAULT (newsequentialid()) FOR politica_id;
ALTER TABLE iam.politica_abac ADD  DEFAULT ((1)) FOR esta_activa;
ALTER TABLE iam.politica_abac ADD  DEFAULT (sysutcdatetime()) FOR creado_en;
ALTER TABLE iam.rol ADD  DEFAULT (newsequentialid()) FOR rol_id;
ALTER TABLE iam.token_actualizacion ADD  DEFAULT (newsequentialid()) FOR token_id;
ALTER TABLE iam.token_actualizacion ADD  DEFAULT (sysutcdatetime()) FOR emitido_el;
ALTER TABLE iam.token_actualizacion ADD  DEFAULT ((0)) FOR revocado;
ALTER TABLE iam.usuario ADD  DEFAULT (newsequentialid()) FOR usuario_id;
ALTER TABLE iam.usuario ADD  DEFAULT ((1)) FOR esta_activo;
ALTER TABLE iam.usuario ADD  DEFAULT (sysutcdatetime()) FOR creado_en;
ALTER TABLE iam.usuario ADD  DEFAULT (sysutcdatetime()) FOR actualizado_en;
ALTER TABLE iam.usuario_rol ADD  DEFAULT (newsequentialid()) FOR usuario_rol_id;
ALTER TABLE iam.usuario_rol ADD  DEFAULT (sysutcdatetime()) FOR asignado_el;
ALTER TABLE mantenimiento.evidencia_ticket ADD  DEFAULT (newsequentialid()) FOR evidencia_id;
ALTER TABLE mantenimiento.evidencia_ticket ADD  DEFAULT ('FOTO') FOR tipo_evidencia;
ALTER TABLE mantenimiento.evidencia_ticket ADD  DEFAULT (sysutcdatetime()) FOR subido_el;
ALTER TABLE mantenimiento.historial_asignacion_ticket ADD  DEFAULT (newsequentialid()) FOR historial_id;
ALTER TABLE mantenimiento.historial_asignacion_ticket ADD  DEFAULT (sysutcdatetime()) FOR asignado_el;
ALTER TABLE mantenimiento.lista_verificacion_ticket ADD  DEFAULT (newsequentialid()) FOR item_id;
ALTER TABLE mantenimiento.lista_verificacion_ticket ADD  DEFAULT ((0)) FOR esta_completado;
ALTER TABLE mantenimiento.lista_verificacion_ticket ADD  DEFAULT ((0)) FOR orden_visualizacion;
ALTER TABLE mantenimiento.politica_sla ADD  DEFAULT (newsequentialid()) FOR politica_sla_id;
ALTER TABLE mantenimiento.politica_sla ADD  DEFAULT ((1)) FOR esta_activa;
ALTER TABLE mantenimiento.politica_sla ADD  DEFAULT (sysutcdatetime()) FOR creado_en;
ALTER TABLE mantenimiento.ticket ADD  DEFAULT (newsequentialid()) FOR ticket_id;
ALTER TABLE mantenimiento.ticket ADD  DEFAULT ('BORRADOR') FOR estado;
ALTER TABLE mantenimiento.ticket ADD  DEFAULT ((0)) FOR sla_incumplido;
ALTER TABLE mantenimiento.ticket ADD  DEFAULT ((0)) FOR conteo_reapertura;
ALTER TABLE mantenimiento.ticket ADD  DEFAULT (sysutcdatetime()) FOR creado_en;
ALTER TABLE mantenimiento.ticket ADD  DEFAULT (sysutcdatetime()) FOR actualizado_en;
ALTER TABLE analitica.kpi_diario  WITH CHECK ADD  CONSTRAINT fk_kpi_diario_inquilino FOREIGN KEY(inquilino_id)
    REFERENCES core.inquilino (inquilino_id);
ALTER TABLE analitica.kpi_diario CHECK CONSTRAINT fk_kpi_diario_inquilino;
ALTER TABLE analitica.kpi_mensual  WITH CHECK ADD  CONSTRAINT fk_kpi_mensual_inquilino FOREIGN KEY(inquilino_id)
    REFERENCES core.inquilino (inquilino_id);
ALTER TABLE analitica.kpi_mensual CHECK CONSTRAINT fk_kpi_mensual_inquilino;
ALTER TABLE analitica.resumen_impacto  WITH CHECK ADD  CONSTRAINT fk_impacto_anomalia FOREIGN KEY(anomalia_id)
    REFERENCES energiaops.anomalia (anomalia_id);
ALTER TABLE analitica.resumen_impacto CHECK CONSTRAINT fk_impacto_anomalia;
ALTER TABLE analitica.resumen_impacto  WITH CHECK ADD  CONSTRAINT fk_impacto_inquilino FOREIGN KEY(inquilino_id)
    REFERENCES core.inquilino (inquilino_id);
ALTER TABLE analitica.resumen_impacto CHECK CONSTRAINT fk_impacto_inquilino;
ALTER TABLE analitica.resumen_impacto  WITH CHECK ADD  CONSTRAINT fk_impacto_ticket FOREIGN KEY(ticket_id)
    REFERENCES mantenimiento.ticket (ticket_id);
ALTER TABLE analitica.resumen_impacto CHECK CONSTRAINT fk_impacto_ticket;
ALTER TABLE audit.log_cambios  WITH CHECK ADD  CONSTRAINT fk_log_cambios_evento FOREIGN KEY(evento_id)
    REFERENCES audit.evento_auditoria (evento_id);
ALTER TABLE audit.log_cambios CHECK CONSTRAINT fk_log_cambios_evento;
ALTER TABLE consumo.importacion_lectura  WITH CHECK ADD  CONSTRAINT fk_importacion_inquilino_inquilino FOREIGN KEY(inquilino_id)
    REFERENCES core.inquilino (inquilino_id);
ALTER TABLE consumo.importacion_lectura CHECK CONSTRAINT fk_importacion_inquilino_inquilino;
ALTER TABLE consumo.lectura  WITH CHECK ADD  CONSTRAINT fk_lectura_inquilino FOREIGN KEY(inquilino_id)
    REFERENCES core.inquilino (inquilino_id);
ALTER TABLE consumo.lectura CHECK CONSTRAINT fk_lectura_inquilino;
ALTER TABLE consumo.lectura  WITH CHECK ADD  CONSTRAINT fk_lectura_medidor FOREIGN KEY(medidor_id)
    REFERENCES core.medidor (medidor_id);
ALTER TABLE consumo.lectura CHECK CONSTRAINT fk_lectura_medidor;
ALTER TABLE core.edificio  WITH CHECK ADD  CONSTRAINT fk_edificio_inquilino FOREIGN KEY(inquilino_id)
    REFERENCES core.inquilino (inquilino_id);
ALTER TABLE core.edificio CHECK CONSTRAINT fk_edificio_inquilino;
ALTER TABLE core.equipo  WITH CHECK ADD  CONSTRAINT fk_equipo_edificio FOREIGN KEY(edificio_id)
    REFERENCES core.edificio (edificio_id);
ALTER TABLE core.equipo CHECK CONSTRAINT fk_edificio_id;
ALTER TABLE core.equipo  WITH CHECK ADD  CONSTRAINT fk_equipo_inquilino FOREIGN KEY(inquilino_id)
    REFERENCES core.inquilino (inquilino_id);
ALTER TABLE core.equipo CHECK CONSTRAINT fk_equipo_inquilino;
ALTER TABLE core.equipo  WITH CHECK ADD  CONSTRAINT fk_equipo_medidor FOREIGN KEY(medidor_id)
    REFERENCES core.medidor (medidor_id);
ALTER TABLE core.equipo CHECK CONSTRAINT fk_equipo_medidor;
ALTER TABLE core.medidor  WITH CHECK ADD  CONSTRAINT fk_medidor_edificio FOREIGN KEY(edificio_id)
    REFERENCES core.edificio (edificio_id);
ALTER TABLE core.medidor CHECK CONSTRAINT fk_medidor_edificio;
ALTER TABLE core.medidor  WITH CHECK ADD  CONSTRAINT fk_medidor_inquilino FOREIGN KEY(inquilino_id)
    REFERENCES core.inquilino (inquilino_id);
ALTER TABLE core.medidor CHECK CONSTRAINT fk_medidor_inquilino;
ALTER TABLE core.tarifa  WITH CHECK ADD  CONSTRAINT fk_tarifa_inquilino FOREIGN KEY(inquilino_id)
    REFERENCES core.inquilino (inquilino_id);
ALTER TABLE core.tarifa CHECK CONSTRAINT fk_tarifa_inquilino;
ALTER TABLE educacion.medalla_usuario  WITH CHECK ADD  CONSTRAINT fk_medalla_usuario_inq FOREIGN KEY(inquilino_id)
    REFERENCES core.inquilino (inquilino_id);
ALTER TABLE educacion.medalla_usuario CHECK CONSTRAINT fk_medalla_usuario_inq;
ALTER TABLE educacion.medalla_usuario  WITH CHECK ADD  CONSTRAINT fk_medalla_usuario_med FOREIGN KEY(medalla_id)
    REFERENCES educacion.medalla (medalla_id);
ALTER TABLE educacion.medalla_usuario CHECK CONSTRAINT fk_medalla_usuario_med;
ALTER TABLE educacion.medalla_usuario  WITH CHECK ADD  CONSTRAINT fk_medalla_usuario_usr FOREIGN KEY(usuario_id)
    REFERENCES iam.usuario (usuario_id);
ALTER TABLE educacion.medalla_usuario CHECK CONSTRAINT fk_medalla_usuario_usr;
ALTER TABLE educacion.progreso_reto  WITH CHECK ADD  CONSTRAINT fk_progreso_inquilino FOREIGN KEY(inquilino_id)
    REFERENCES core.inquilino (inquilino_id);
ALTER TABLE educacion.progreso_reto CHECK CONSTRAINT fk_progreso_inquilino;
ALTER TABLE educacion.progreso_reto  WITH CHECK ADD  CONSTRAINT fk_progreso_reto_ref FOREIGN KEY(reto_id)
    REFERENCES educacion.reto (reto_id);
ALTER TABLE educacion.progreso_reto CHECK CONSTRAINT fk_progreso_reto_ref;
ALTER TABLE educacion.progreso_reto  WITH CHECK ADD  CONSTRAINT fk_progreso_usuario FOREIGN KEY(usuario_id)
    REFERENCES iam.usuario (usuario_id);
ALTER TABLE educacion.progreso_reto CHECK CONSTRAINT fk_progreso_usuario;
ALTER TABLE educacion.reto  WITH CHECK ADD  CONSTRAINT fk_reto_creador FOREIGN KEY(creado_por)
    REFERENCES iam.usuario (usuario_id);
ALTER TABLE educacion.reto CHECK CONSTRAINT fk_reto_creador;
ALTER TABLE educacion.reto  WITH CHECK ADD  CONSTRAINT fk_reto_edificio FOREIGN KEY(edificio_id)
    REFERENCES core.edificio (edificio_id);
ALTER TABLE educacion.reto CHECK CONSTRAINT fk_reto_edificio;
ALTER TABLE educacion.reto  WITH CHECK ADD  CONSTRAINT fk_reto_inquilino FOREIGN KEY(inquilino_id)
    REFERENCES core.inquilino (inquilino_id);
ALTER TABLE educacion.reto CHECK CONSTRAINT fk_reto_inquilino;
ALTER TABLE educacion.snapshot_ranking  WITH CHECK ADD  CONSTRAINT fk_ranking_inquilino FOREIGN KEY(inquilino_id)
    REFERENCES core.inquilino (inquilino_id);
ALTER TABLE educacion.snapshot_ranking CHECK CONSTRAINT fk_ranking_inquilino;
ALTER TABLE educacion.snapshot_ranking  WITH CHECK ADD  CONSTRAINT fk_ranking_reto FOREIGN KEY(reto_id)
    REFERENCES educacion.reto (reto_id);
ALTER TABLE educacion.snapshot_ranking CHECK CONSTRAINT fk_ranking_reto;
ALTER TABLE educacion.snapshot_ranking  WITH CHECK ADD  CONSTRAINT fk_ranking_usuario FOREIGN KEY(usuario_id)
    REFERENCES iam.usuario (usuario_id);
ALTER TABLE educacion.snapshot_ranking CHECK CONSTRAINT fk_ranking_usuario;
ALTER TABLE energiaops.anomalia  WITH CHECK ADD  CONSTRAINT fk_anomalia_inquilino FOREIGN KEY(inquilino_id)
    REFERENCES core.inquilino (inquilino_id);
ALTER TABLE energiaops.anomalia CHECK CONSTRAINT fk_anomalia_inquilino;
ALTER TABLE energiaops.anomalia  WITH CHECK ADD  CONSTRAINT fk_anomalia_lectura FOREIGN KEY(lectura_id)
    REFERENCES consumo.lectura (lectura_id);
ALTER TABLE energiaops.anomalia CHECK CONSTRAINT fk_anomalia_lectura;
ALTER TABLE energiaops.anomalia  WITH CHECK ADD  CONSTRAINT fk_anomalia_medidor FOREIGN KEY(medidor_id)
    REFERENCES core.medidor (medidor_id);
ALTER TABLE energiaops.anomalia CHECK CONSTRAINT fk_anomalia_medidor;
ALTER TABLE energiaops.log_prediccion_ia  WITH CHECK ADD  CONSTRAINT fk_ia_pred_inquilino FOREIGN KEY(inquilino_id)
    REFERENCES core.inquilino (inquilino_id);
ALTER TABLE energiaops.log_prediccion_ia CHECK CONSTRAINT fk_ia_pred_inquilino;
ALTER TABLE energiaops.log_prediccion_ia  WITH CHECK ADD  CONSTRAINT fk_ia_pred_medidor FOREIGN KEY(medidor_id)
    REFERENCES core.medidor (medidor_id);
ALTER TABLE energiaops.log_prediccion_ia CHECK CONSTRAINT fk_ia_pred_medidor;
ALTER TABLE energiaops.snapshot_linea_base  WITH CHECK ADD  CONSTRAINT fk_linea_base_inquilino FOREIGN KEY(inquilino_id)
    REFERENCES core.inquilino (inquilino_id);
ALTER TABLE energiaops.snapshot_linea_base CHECK CONSTRAINT fk_linea_base_inquilino;
ALTER TABLE energiaops.snapshot_linea_base  WITH CHECK ADD  CONSTRAINT fk_linea_base_medidor FOREIGN KEY(medidor_id)
    REFERENCES core.medidor (medidor_id);
ALTER TABLE energiaops.snapshot_linea_base CHECK CONSTRAINT fk_linea_base_medidor;
ALTER TABLE iam.politica_abac  WITH CHECK ADD  CONSTRAINT fk_politica_abac_rol FOREIGN KEY(rol_id)
    REFERENCES iam.rol (rol_id);
ALTER TABLE iam.politica_abac CHECK CONSTRAINT fk_politica_abac_rol;
ALTER TABLE iam.rol_permiso  WITH CHECK ADD  CONSTRAINT fk_rol_permiso_perm FOREIGN KEY(permiso_id)
    REFERENCES iam.permiso (permiso_id);
ALTER TABLE iam.rol_permiso CHECK CONSTRAINT fk_rol_permiso_perm;
ALTER TABLE iam.rol_permiso  WITH CHECK ADD  CONSTRAINT fk_rol_permiso_rol FOREIGN KEY(rol_id)
    REFERENCES iam.rol (rol_id);
ALTER TABLE iam.rol_permiso CHECK CONSTRAINT fk_rol_permiso_rol;
ALTER TABLE iam.token_actualizacion  WITH CHECK ADD  CONSTRAINT fk_token_actualizacion_usuario FOREIGN KEY(usuario_id)
    REFERENCES iam.usuario (usuario_id);
ALTER TABLE iam.token_actualizacion CHECK CONSTRAINT fk_token_actualizacion_usuario;
ALTER TABLE iam.usuario  WITH CHECK ADD  CONSTRAINT fk_usuario_inquilino FOREIGN KEY(inquilino_id)
    REFERENCES core.inquilino (inquilino_id);
ALTER TABLE iam.usuario CHECK CONSTRAINT fk_usuario_inquilino;
ALTER TABLE iam.usuario_rol  WITH CHECK ADD  CONSTRAINT fk_usuario_rol_edificio FOREIGN KEY(edificio_id)
    REFERENCES core.edificio (edificio_id);
ALTER TABLE iam.usuario_rol CHECK CONSTRAINT fk_usuario_rol_edificio;
ALTER TABLE iam.usuario_rol  WITH CHECK ADD  CONSTRAINT fk_usuario_rol_inquilino FOREIGN KEY(inquilino_id)
    REFERENCES core.inquilino (inquilino_id);
ALTER TABLE iam.usuario_rol CHECK CONSTRAINT fk_usuario_rol_inquilino;
ALTER TABLE iam.usuario_rol  WITH CHECK ADD  CONSTRAINT fk_usuario_rol_rol FOREIGN KEY(rol_id)
    REFERENCES iam.rol (rol_id);
ALTER TABLE iam.usuario_rol CHECK CONSTRAINT fk_usuario_rol_rol;
ALTER TABLE iam.usuario_rol  WITH CHECK ADD  CONSTRAINT fk_usuario_rol_usuario FOREIGN KEY(usuario_id)
    REFERENCES iam.usuario (usuario_id);
ALTER TABLE iam.usuario_rol CHECK CONSTRAINT fk_usuario_rol_usuario;
ALTER TABLE mantenimiento.evidencia_ticket  WITH CHECK ADD  CONSTRAINT fk_evidencia_inquilino FOREIGN KEY(inquilino_id)
    REFERENCES core.inquilino (inquilino_id);
ALTER TABLE mantenimiento.evidencia_ticket CHECK CONSTRAINT fk_evidencia_inquilino;
ALTER TABLE mantenimiento.evidencia_ticket  WITH CHECK ADD  CONSTRAINT fk_evidencia_ticket_ref FOREIGN KEY(ticket_id)
    REFERENCES mantenimiento.ticket (ticket_id);
ALTER TABLE mantenimiento.evidencia_ticket CHECK CONSTRAINT fk_evidencia_ticket_ref;
ALTER TABLE mantenimiento.evidencia_ticket  WITH CHECK ADD  CONSTRAINT fk_evidencia_usuario FOREIGN KEY(subido_por)
    REFERENCES iam.usuario (usuario_id);
ALTER TABLE mantenimiento.evidencia_ticket CHECK CONSTRAINT fk_evidencia_usuario;
ALTER TABLE mantenimiento.historial_asignacion_ticket  WITH CHECK ADD  CONSTRAINT fk_hist_asign_inquilino FOREIGN KEY(inquilino_id)
    REFERENCES core.inquilino (inquilino_id);
ALTER TABLE mantenimiento.historial_asignacion_ticket CHECK CONSTRAINT fk_hist_asign_inquilino;
ALTER TABLE mantenimiento.historial_asignacion_ticket  WITH CHECK ADD  CONSTRAINT fk_hist_asign_ticket FOREIGN KEY(ticket_id)
    REFERENCES mantenimiento.ticket (ticket_id);
ALTER TABLE mantenimiento.historial_asignacion_ticket CHECK CONSTRAINT fk_hist_asign_ticket;
ALTER TABLE mantenimiento.lista_verificacion_ticket  WITH CHECK ADD  CONSTRAINT fk_checklist_inquilino FOREIGN KEY(inquilino_id)
    REFERENCES core.inquilino (inquilino_id);
ALTER TABLE mantenimiento.lista_verificacion_ticket CHECK CONSTRAINT fk_checklist_inquilino;
ALTER TABLE mantenimiento.lista_verificacion_ticket  WITH CHECK ADD  CONSTRAINT fk_checklist_ticket FOREIGN KEY(ticket_id)
    REFERENCES mantenimiento.ticket (ticket_id);
ALTER TABLE mantenimiento.lista_verificacion_ticket CHECK CONSTRAINT fk_checklist_ticket;
ALTER TABLE mantenimiento.politica_sla  WITH CHECK ADD  CONSTRAINT fk_politica_sla_inquilino FOREIGN KEY(inquilino_id)
    REFERENCES core.inquilino (inquilino_id);
ALTER TABLE mantenimiento.politica_sla CHECK CONSTRAINT fk_politica_sla_inquilino;
ALTER TABLE mantenimiento.ticket  WITH CHECK ADD  CONSTRAINT fk_ticket_anomalia FOREIGN KEY(anomalia_id)
    REFERENCES energiaops.anomalia (anomalia_id);
ALTER TABLE mantenimiento.ticket CHECK CONSTRAINT fk_ticket_anomalia;
ALTER TABLE mantenimiento.ticket  WITH CHECK ADD  CONSTRAINT fk_ticket_asignado FOREIGN KEY(asignado_a)
    REFERENCES iam.usuario (usuario_id);
ALTER TABLE mantenimiento.ticket CHECK CONSTRAINT fk_ticket_asignado;
ALTER TABLE mantenimiento.ticket  WITH CHECK ADD  CONSTRAINT fk_ticket_creador FOREIGN KEY(creado_por)
    REFERENCES iam.usuario (usuario_id);
ALTER TABLE mantenimiento.ticket CHECK CONSTRAINT fk_ticket_creador;
ALTER TABLE mantenimiento.ticket  WITH CHECK ADD  CONSTRAINT fk_ticket_edificio FOREIGN KEY(edificio_id)
    REFERENCES core.edificio (edificio_id);
ALTER TABLE mantenimiento.ticket CHECK CONSTRAINT fk_ticket_edificio;
ALTER TABLE mantenimiento.ticket  WITH CHECK ADD  CONSTRAINT fk_ticket_inquilino FOREIGN KEY(inquilino_id)
    REFERENCES core.inquilino (inquilino_id);
ALTER TABLE mantenimiento.ticket CHECK CONSTRAINT fk_ticket_inquilino;
ALTER TABLE mantenimiento.ticket  WITH CHECK ADD  CONSTRAINT fk_ticket_sla FOREIGN KEY(politica_sla_id)
    REFERENCES mantenimiento.politica_sla (politica_sla_id);
ALTER TABLE mantenimiento.ticket CHECK CONSTRAINT fk_ticket_sla;
ALTER TABLE analitica.kpi_mensual  WITH CHECK ADD  CONSTRAINT chk_kpi_mes CHECK  ((mes_kpi>=(1) AND mes_kpi<=(12)));
ALTER TABLE analitica.kpi_mensual CHECK CONSTRAINT chk_kpi_mes;
ALTER TABLE audit.evento_auditoria  WITH CHECK ADD  CONSTRAINT chk_auditoria_severidad CHECK  ((severidad='CRITICA' OR severidad='ALTA' OR severidad='MEDIA' OR severidad='BAJA'));
ALTER TABLE audit.evento_auditoria CHECK CONSTRAINT chk_auditoria_severidad;
ALTER TABLE consumo.importacion_lectura  WITH CHECK ADD  CONSTRAINT chk_importacion_estado CHECK  ((estado='FALLIDO' OR estado='COMPLETADO' OR estado='PROCESANDO'));
ALTER TABLE consumo.importacion_lectura CHECK CONSTRAINT chk_importacion_estado;
ALTER TABLE consumo.lectura  WITH CHECK ADD  CONSTRAINT chk_lectura_estado CHECK  ((estado='ANULADA' OR estado='RECHAZADA' OR estado='VALIDADA' OR estado='PENDIENTE'));
ALTER TABLE consumo.lectura CHECK CONSTRAINT chk_lectura_estado;
ALTER TABLE consumo.lectura  WITH CHECK ADD  CONSTRAINT chk_lectura_origen CHECK  ((origen='API' OR origen='IOT' OR origen='IMPORT' OR origen='MANUAL'));
ALTER TABLE consumo.lectura CHECK CONSTRAINT chk_lectura_origen;
ALTER TABLE consumo.lectura  WITH CHECK ADD  CONSTRAINT chk_lectura_positiva CHECK  ((valor>=(0)));
ALTER TABLE consumo.lectura CHECK CONSTRAINT chk_lectura_positiva;
ALTER TABLE core.inquilino  WITH CHECK ADD  CONSTRAINT chk_inquilino_plan CHECK  ((tipo_plan='ENTERPRISE' OR tipo_plan='PRO' OR tipo_plan='BASIC'));
ALTER TABLE core.inquilino CHECK CONSTRAINT chk_inquilino_plan;
ALTER TABLE core.medidor  WITH CHECK ADD  CONSTRAINT chk_tipo_medidor CHECK  ((tipo_medidor='GAS' OR tipo_medidor='AGUA' OR tipo_medidor='ELECTRICIDAD'));
ALTER TABLE core.medidor CHECK CONSTRAINT chk_tipo_medidor;
ALTER TABLE educacion.reto  WITH CHECK ADD  CONSTRAINT chk_reto_estado CHECK  ((estado='EXPIRADO' OR estado='COMPLETADO' OR estado='EN_PROGRESO' OR estado='ASIGNADO' OR estado='CREADO'));
ALTER TABLE educacion.reto CHECK CONSTRAINT chk_reto_estado;
ALTER TABLE energiaops.anomalia  WITH CHECK ADD  CONSTRAINT chk_anomalia_estado CHECK  ((estado='IGNORADA' OR estado='RESUELTA' OR estado='EN_ACCION' OR estado='NOTIFICADA' OR estado='DETECTADA'));
ALTER TABLE energiaops.anomalia CHECK CONSTRAINT chk_anomalia_estado;
ALTER TABLE energiaops.anomalia  WITH CHECK ADD  CONSTRAINT chk_anomalia_severidad CHECK  ((severidad='CRITICA' OR severidad='ALTA' OR severidad='MEDIA' OR severidad='BAJA'));
ALTER TABLE energiaops.anomalia CHECK CONSTRAINT chk_anomalia_severidad;
ALTER TABLE energiaops.anomalia  WITH CHECK ADD  CONSTRAINT chk_anomalia_tipo CHECK  ((tipo_anomalia='OTRO' OR tipo_anomalia='SUBIDA_RAPIDA' OR tipo_anomalia='LINEA_PLANA' OR tipo_anomalia='NOCTURNO' OR tipo_anomalia='PICO'));
ALTER TABLE energiaops.anomalia CHECK CONSTRAINT chk_anomalia_tipo;
ALTER TABLE energiaops.snapshot_linea_base  WITH CHECK ADD  CONSTRAINT chk_linea_base_periodo CHECK  ((tipo_periodo='MENSUAL' OR tipo_periodo='SEMANAL' OR tipo_periodo='DIARIO' OR tipo_periodo='HORARIO'));
ALTER TABLE energiaops.snapshot_linea_base CHECK CONSTRAINT chk_linea_base_periodo;
ALTER TABLE mantenimiento.evidencia_ticket  WITH CHECK ADD  CONSTRAINT chk_tipo_evidencia CHECK  ((tipo_evidencia='NOTA' OR tipo_evidencia='DOCUMENTO' OR tipo_evidencia='VIDEO' OR tipo_evidencia='FOTO'));
ALTER TABLE mantenimiento.evidencia_ticket CHECK CONSTRAINT chk_tipo_evidencia;
ALTER TABLE mantenimiento.ticket  WITH CHECK ADD  CONSTRAINT chk_ticket_estado CHECK  ((estado='REABIERTO' OR estado='CERRADO' OR estado='EN_PROGRESO' OR estado='ASIGNADO' OR estado='ABIERTO' OR estado='BORRADOR'));
ALTER TABLE mantenimiento.ticket CHECK CONSTRAINT chk_ticket_estado;
ALTER TABLE mantenimiento.ticket  WITH CHECK ADD  CONSTRAINT chk_ticket_prioridad CHECK  ((prioridad='CRITICA' OR prioridad='ALTA' OR prioridad='MEDIA' OR prioridad='BAJA'));
ALTER TABLE mantenimiento.ticket CHECK CONSTRAINT chk_ticket_prioridad;

IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_NAME = 'snapshot_linea_base'
    AND TABLE_SCHEMA = 'energiaops'
    AND COLUMN_NAME = 'activo'
)
BEGIN
ALTER TABLE energiaops.snapshot_linea_base
    ADD activo BIT NOT NULL DEFAULT (1);
PRINT 'Columna activo agregada correctamente.';
END
ELSE
BEGIN
    PRINT 'La columna activo ya existe.';
END;
