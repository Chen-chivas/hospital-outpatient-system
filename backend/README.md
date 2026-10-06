# Backend

Spring Boot backend for the integrated hospital outpatient management system.

## Run

```bash
mvn spring-boot:run
```

Default port: `8080`.

## Database

The backend uses one unified H2 file database:

```text
jdbc:h2:file:./.data/ocs;AUTO_SERVER=TRUE;MODE=MySQL
```

All integrated modules use the same `spring.datasource` connection:

- Core JPA modules
- Outpatient MyBatis-Plus module
- Smart medical MyBatis module

Flyway automatically creates and migrates the schema from:

```text
src/main/resources/db/migration
```

## Useful APIs

- `GET /api/ping`
- `POST /api/auth/login`
- `GET /api/users`
- `GET /api/schedules`
- `POST /api/registrations/book`
- `GET /api/bills/my`
- `POST /api/visits/start`
- `GET /api/prescriptions`
- `POST /api/pharmacy/dispense`
- `GET /api/statistics/summary`

## Demo Accounts

- Admin: `admin` / `admin123`
- Doctor: `doctor1` / `doctor123`
- Patient: `patient1` / `patient123`
- Cashier: `cashier1` / `cashier123`
- Pharmacist: `pharmacist1` / `pharmacist123`
