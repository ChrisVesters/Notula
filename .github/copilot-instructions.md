Project: Notula — meeting notes web app (Java Spring Boot backend + SvelteKit frontend)

Quick context
- Backend: Spring Boot app in `backend/` (Java 24, Maven). REST API under `/api/*`, Postgres persistence, Flyway for migrations.
- Frontend: SvelteKit app in `frontend/` (Vite, TypeScript). Calls backend via `VITE_API_URL`.

What to do first
- Read `backend/src/main/java/com/cvesters/notula` for controllers and services. Key controllers: `UserController`, `OrganisationController`.
- Read `frontend/src/lib/user/UserClient.ts` and `frontend/src/lib/user/UserTypes.ts` for client-side API shapes.

Architecture & conventions (important for making changes)
- API surface: backend exposes REST endpoints under `/api/*`. Example: `POST /api/users` handled by `UserController#create` which expects `CreateUserDto` and returns `UserInfoDto`.
- Service layer: controllers delegate to `*Service` classes (e.g., `UserService`) for business logic.
- Persistence layer: `*StorageGateway` classes wrap Spring Data repositories and enforce domain conversions (DAO <-> BDO). Look at `UserStorageGateway` and `OrganisationStorageGateway`.
- Exception handling: centralized in `common.controller.HttpExceptionHandler` (ControllerAdvice). Prefer throwing domain exceptions (e.g., `DuplicateEntityException`) and let the handler map to HTTP status codes.
- DTO/BDO/DAO pattern: the project uses DTOs for transport, BDOs (business domain objects) for service boundaries, and DAOs for JPA entities. Keep conversions explicit (see `*.dto.*`, `*.bdo.*`, `*.dao.*`).
- Security: Spring Security is declared in `pom.xml` and password hashing uses a `PasswordEncoder` in `UserStorageGateway`. Tests include `spring-security-test`.

Build / test / dev workflows (commands to run)
- Backend (from repo root):
  - Build & tests: `./mvnw -f backend clean verify` (runs unit tests, prepares JaCoCo)
  - Run app (dev): `./mvnw -f backend spring-boot:run` (app listens on port configured in `backend/src/main/resources/application.properties`, default 7000)
  - Integration tests use Testcontainers (see `TestContainerConfig`) and expect Docker available.
- Frontend (from repo root):
  - Install deps: `cd frontend && npm install`
  - Dev server: `npm run dev` (Vite). Frontend reads `VITE_API_URL` to target backend.
  - Unit tests: `npm run test` (Vitest). Formatting/linting: `npm run format` / `npm run lint`.

Project-specific patterns and gotchas
- Hard-coded server port: backend default is `server.port=7000` in `application.properties`. When running frontend dev, set `VITE_API_URL` to `http://localhost:7000/api` to reach backend endpoints.
- DTO validation: controllers use Jakarta Validation (`@Valid`) on DTOs — return 400 for validation failures via the exception handler.
- Response creation: controllers commonly return created responses using `ServletUriComponentsBuilder` (see `UserController#create`) — maintain this pattern for POST endpoints that create resources.
- Password handling: `UserStorageGateway` encodes raw passwords with a `PasswordEncoder` before saving; never store plain passwords.
- Database migrations: Flyway is present; check `backend/src/main/resources/db/migration` (if present) before modifying schema.
- Tests & containers: integration tests rely on Testcontainers and the `@ServiceConnection` bean; ensure Docker is running for these tests.

Files to consult when making changes
- Backend: `backend/pom.xml` (dependencies/plugins), `backend/src/main/resources/application.properties`, `backend/src/main/java/com/cvesters/notula/**` (controllers, services, storage, dto/bdo/dao packages).
- Frontend: `frontend/package.json` (scripts), `frontend/src/lib/user/UserClient.ts` (API client), `frontend/src/routes` (pages and layouts).

Examples to copy or follow
- Adding a new POST resource: create DTO in `*.dto`, BDO in `*.bdo`, Service method in `*Service`, and persistence in `*StorageGateway` and repository. Mirror `UserController#create` and `UserStorageGateway#createUser`.
- API client: follow `UserClient.create` for fetch usage and `VITE_API_URL` usage.

Other notes
- Java version: project targets Java 24 in Maven properties — local developers should use a compatible JDK.
- Keep exception mapping in `HttpExceptionHandler` consistent; add mappings for new domain exceptions here.

If anything is missing or unclear, ask for the specific goal (e.g., "add endpoint X", "change DB schema") and I will point to exact files and tests to update.
