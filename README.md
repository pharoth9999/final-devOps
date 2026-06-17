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

## Running locally

1. Start MySQL: `docker compose up -d` (or point at your own instance).
2. Run the app: `./mvnw spring-boot:run`
3. Open `http://localhost:8080` for the live-preview demo page.

Configuration is environment-driven (see `application.properties`):
`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`, `UPLOAD_DIR`,
`PUBLIC_BASE_URL`.

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
