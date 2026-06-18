# Cloud API-Based Network Application Security for Small Company Services

Monorepo structure for the project:

- `backend/`: Spring Boot REST API, Spring Security, JWT, OAuth2, bcrypt, AES.
- `frontend/`: NextJS user interface.
- `database/`: SQL schema and seed data.
- `docs/`: report, screenshots, Postman collection, OWASP ZAP results.
- `deploy/`: deployment and reverse proxy configuration.

Main Java package:

```text
com.company.securityapp
```

Local ports:

- `backend`: `8080`
- `frontend`: `3000`

Backend foundation status:

- Spring Boot `3.3.x`
- Java `17`
- Spring Web, Spring Security, Spring Data JPA, Validation
- MySQL driver for project database
- H2 profile for local scaffolding and quick boot
- Swagger/OpenAPI at `/swagger-ui.html`

Run backend locally:

```bash
cd backend
mvn clean package -DskipTests
mvn spring-boot:run
```

Useful backend URLs after boot:

- `http://localhost:8080/api/health`
- `http://localhost:8080/swagger-ui.html`
- `http://localhost:8080/v3/api-docs`

Run backend with MySQL profile:

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Optional environment variables for MySQL profile:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `APP_JWT_SECRET`
- `APP_AES_SECRET`
