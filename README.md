# ID Card Manager

A Spring Boot application for managing ID cards for students, employees, and
general users: profile CRUD, photo uploads, a Thymeleaf-based card template
engine with live preview, unique registration number generation, PDF export
(iText), batch generation, QR codes (ZXing) and Code-128 / EAN-13 barcodes.

## Stack

- Java 21, Spring Boot 3.5 (Web, Data JPA, Validation, Thymeleaf)
- MySQL (runtime), H2 (tests)
- ZXing for QR codes and linear barcodes
- iText `html2pdf` for PDF export (reuses the same Thymeleaf-rendered HTML
  used for the live preview, so the PDF and the on-screen card always match)

## Project layout

```
src/main/java/com/example/demo/
  model/        Profile, ProfileType, ProfileBuilder, Template, BarcodeType
  repository/   ProfileRepository, TemplateRepository
  service/      ProfileService, TemplateService, RegistrationNumberGenerator,
                PhotoStorageService, QrCodeService, BarcodeService,
                IdCardRenderService, PdfExportService
  controller/   ProfileController, TemplateController
  config/       DataSeeder (seeds a default card Template)
  exception/    ResourceNotFoundException, InvalidPhotoException, handler
src/main/resources/
  templates/    idcard.html (single card fragment), idcard-batch.html
  static/       index.html — minimal live-preview demo UI
```

## Running locally (without Docker)

1. Start MySQL: point at your own local instance, or run just the database
   service from the compose file: `docker compose up -d mysql`.
2. Run the app: `./mvnw spring-boot:run`
3. Open `http://localhost:8080` for the live-preview demo page.

Configuration is environment-driven (see `application.properties`):
`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`, `UPLOAD_DIR`,
`PUBLIC_BASE_URL`.

## Running with Docker Compose (full deployment)

Two containers:

- **web** — built from the `Dockerfile`: JDK 21, Maven and Git (used to build
  the app), NGINX (reverse proxy) and OpenSSH server, all in one image.
  - The Spring Boot app runs **internally** on port 8081.
  - NGINX listens on port 8080 inside the container and proxies to
    `127.0.0.1:8081`.
  - Host port `8443` → container port `8080` (website).
  - Host port `2222` → container port `22` (SSH, root password `Hello@123`).
- **mysql** — official `mysql:8` image.
  - Database `A-ThyPharoth-db`, user `root`, password `Hello@123`.
  - Host port `3306` → container port `3306`.
  - Data persisted in the named volume `idcard-mysql-data`.

The `web` container connects to MySQL via the Docker network using
`jdbc:mysql://mysql:3306/A-ThyPharoth-db`, with credentials supplied through
`SPRING_DATASOURCE_URL` / `SPRING_DATASOURCE_USERNAME` /
`SPRING_DATASOURCE_PASSWORD` environment variables set in
`docker-compose.yml`. The web container's startup script
(`docker/start.sh`) waits for MySQL to accept TCP connections before
starting the Spring Boot app.

### Commands

Build and start both containers:

```
docker compose up --build
```

Check both containers are running:

```
docker ps
```

Test the website (proxied through NGINX to Spring Boot):

```
curl http://localhost:8443
```

SSH into the web container:

```
ssh root@localhost -p 2222
# password: Hello@123
```

Connect to MySQL inside the database container:

```
docker exec -it idcard-mysql mysql -uroot -p
# password: Hello@123
```

Then, inside the MySQL prompt, confirm the database exists:

```sql
SHOW DATABASES;
```

You should see `A-ThyPharoth-db` in the list.

Stop and remove the containers:

```
docker compose down
```

## Tests

```
./mvnw test
```

Repository tests run against an in-memory H2 database
(`src/test/resources/application.properties`); service/controller tests use
Mockito and `@WebMvcTest`.

## API overview

| Method | Path | Purpose |
|---|---|---|
| GET | `/api/profiles` | search/list profiles (paged) |
| GET | `/api/profiles/{id}` | get one profile |
| GET | `/api/profiles/default?type=` | build a default (unsaved) profile |
| POST | `/api/profiles` | create a profile (uuid + registration number auto-assigned) |
| POST | `/api/profiles/batch` | batch-create profiles |
| PUT | `/api/profiles/{id}` | update a profile |
| DELETE | `/api/profiles/{id}` | delete a profile |
| POST | `/api/profiles/{id}/photo` | upload a JPEG/PNG photo (multipart) |
| GET | `/api/profiles/{id}/photo` | fetch the stored photo |
| GET | `/api/profiles/{id}/preview` | render the saved profile's ID card as HTML |
| POST | `/api/profiles/preview` | instant preview of a draft (unsaved) profile |
| GET | `/api/profiles/{id}/qrcode` | QR code PNG |
| GET | `/api/profiles/{id}/barcode` | Code-128 / EAN-13 barcode PNG |
| GET | `/api/profiles/verify/{uuid}` | verification lookup (what the QR code links to) |
| GET | `/api/profiles/{id}/pdf` | export a single ID card as PDF |
| POST | `/api/profiles/batch/pdf` | merge several profiles' cards into one PDF |
| GET/POST/PUT/DELETE | `/api/templates/**` | manage card templates |

## Submission screenshots (final exam)

1. **File structure** — project tree showing `Dockerfile`, `docker-compose.yml`,
   `docker/nginx.conf`, `docker/start.sh`.
2. **`docker compose up --build` output** — full build + startup log showing
   both containers starting successfully.
3. **`docker ps` output** — showing both `idcard-web` and `idcard-mysql`
   containers `Up`, with their port mappings (`8443->8080`, `2222->22`,
   `3306->3306`).
4. **Website result** — browser or `curl http://localhost:8443` showing the
   app responding.
5. **MySQL `SHOW DATABASES;`** — output from
   `docker exec -it idcard-mysql mysql -uroot -p` showing `A-ThyPharoth-db`
   in the list.
