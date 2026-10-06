# Hospital Outpatient Management System

This repository is now configured as one integrated Spring Boot project.

## Start

Use the integrated startup script from the repository root:

```bat
start-integrated.cmd
```

Or start manually:

```bash
cd backend
mvn -DskipTests package
java -jar target/ocs-backend-0.1.0-SNAPSHOT.jar
```

## URLs

- Vue console: `http://localhost:8080`
- Backend health API: `http://localhost:8080/api/ping`
- Outpatient module: `http://localhost:8080/outpatient`
- Smart medical module: `http://localhost:8080/smart`
- H2 console: `http://localhost:8080/h2-console`

## Unified Environment

The integrated backend uses one runtime database and one runtime process:

| Item | Value |
| --- | --- |
| Runtime process | `backend` Spring Boot app |
| Runtime database | H2 file database |
| JDBC URL | `jdbc:h2:file:./.data/ocs;AUTO_SERVER=TRUE;MODE=MySQL` |
| Username | `sa` |
| Password | empty |
| Schema management | Flyway migrations in `backend/src/main/resources/db/migration` |

The legacy standalone directories and SQL files are kept for reference, but the integrated startup no longer connects to separate MySQL databases named `hospital` or `smart_medical`.

## Java

Spring Boot 3.5 requires Java 17 or newer. The startup script prefers:

```text
E:\Program Files\Java\jdk-21
```

## Frontend Static Assets

The Vue frontend is served by the same backend process from `backend/src/main/resources/static`.

After changing frontend code, rebuild and copy it with:

```bat
start-integrated.cmd -BuildFrontend
```

## Demo Accounts

- Admin: `admin` / `admin123`
- Doctor: `doctor1` / `doctor123`
- Patient: `patient1` / `patient123`
- Cashier: `cashier1` / `cashier123`
- Pharmacist: `pharmacist1` / `pharmacist123`
