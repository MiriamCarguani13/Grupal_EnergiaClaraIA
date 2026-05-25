# Runbook Local

## Prerrequisitos

- Java 21 o superior disponible en `PATH`.
- Maven 3.9 o superior.
- SQL Server escuchando en `localhost:1433`.
- Base de datos `EnergiaClaraDB` creada con `database/script.sql` y `database/seeds.sql`.

## Variables de entorno

El backend tiene defaults locales en `backend/src/main/resources/application.yml`. Si tu SQL Server usa otros valores, exportalos antes de arrancar:

```bash
export DB_URL='jdbc:sqlserver://localhost:1433;databaseName=EnergiaClaraDB;encrypt=false;trustServerCertificate=true'
export DB_USERNAME='sa'
export DB_PASSWORD='Energia2026!'
export JWT_SECRET='energiaclara-super-secret-key-32-chars-minimum!!'
```

## Preparar base de datos

Desde la raiz del proyecto, usando `sqlcmd`:

```bash
sqlcmd -S localhost,1433 -U sa -P 'Energia2026!' -Q "IF DB_ID('EnergiaClaraDB') IS NULL CREATE DATABASE EnergiaClaraDB"
sqlcmd -S localhost,1433 -U sa -P 'Energia2026!' -d EnergiaClaraDB -i database/script.sql
sqlcmd -S localhost,1433 -U sa -P 'Energia2026!' -d EnergiaClaraDB -i database/seeds.sql
```

Si usas SSMS o Azure Data Studio, ejecuta primero `database/script.sql` y despues `database/seeds.sql` sobre `EnergiaClaraDB`.

## Verificar backend

```bash
cd backend
mvn clean test
```

Resultado esperado:

```text
BUILD SUCCESS
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
```

## Ejecutar backend

```bash
cd backend
mvn spring-boot:run
```

Resultado esperado:

```text
HikariPool-1 - Start completed.
Tomcat started on port 8080 (http) with context path ''
Started EnergiaclaraApplication
```

Backend disponible en:

```text
http://localhost:8080
```

## Credenciales demo

```text
tenantId: 11111111-1111-1111-1111-111111111111
email: admin@demo.edu
password: Admin1234!
```

## Login de prueba

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{
    "tenantId": "11111111-1111-1111-1111-111111111111",
    "email": "admin@demo.edu",
    "password": "Admin1234!"
  }'
```

## Problemas frecuentes

- `No se pudo realizar la conexion TCP/IP al host localhost, puerto 1433`: SQL Server no esta corriendo, TCP/IP no esta habilitado, el puerto no es `1433` o las credenciales no coinciden.
- `Login failed for user 'sa'`: ajustar `DB_USERNAME`/`DB_PASSWORD` o habilitar autenticacion SQL Server.
- `Address already in use` en `8080`: detener el proceso que usa el puerto o ejecutar con `mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081`.
- `The database EnergiaClaraDB does not exist`: crear la base y ejecutar los scripts indicados arriba.
