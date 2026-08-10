Notula
=

Notula is a web application for running meetings that actually get somewhere.

Instead of one person typing minutes into a document and mailing them around
afterwards, everyone in the meeting works in the same live agenda. You prepare
the topics up front, give each one a time budget, and take notes underneath it
while the meeting is happening. Every change is shared with the other
participants in real time, so the notes are already finished when the meeting
ends.

Notula is multi-tenant: users belong to one or more organisations, and all
meetings, agendas and notes live inside an organisation.

Where the project is heading is described in [ROADMAP.md](ROADMAP.md).

Features
==

- **Organisations** — group users in an organisation; members are either
  `ADMIN` or `MEMBER`. Users pick (or switch) the organisation they are working
  in, and a session is scoped to that organisation.
- **Meetings** — create meetings with a name and description, list them and
  delete them.
- **Agenda** — a meeting holds an ordered list of topics. Each topic has a
  name, a description and an optional duration, so the agenda doubles as a time
  budget for the meeting.
- **Notes** — every topic contains an ordered list of blocks holding the notes
  taken for that topic. Text blocks are supported today; the block model is
  built to be extended with other block types.
- **Real-time collaboration** — meetings, agendas and notes are edited over a
  WebSocket connection. Edits are sent as incremental text updates
  (position/length/value) rather than whole documents, and are broadcast to
  everyone subscribed to the meeting.
- **Accounts and sessions** — registration with email and password, JWT access
  tokens with a refresh token in an HTTP-only cookie.
- **Internationalisation** — all UI text goes through translation files
  (English is included).

Architecture
==

Notula is split into two applications plus a local nginx that terminates TLS
and puts both behind one origin.

Backend (`backend/`)
===

Java 25 / Spring Boot 4 application, built with Maven.

- REST API under `/api/*` for the request/response parts: `users`, `sessions`,
  `organisations`, `organisation-users` and `meetings`.
- STOMP over WebSocket on `/ws` for everything that happens inside a meeting.
  Clients send to `/app/meetings/{meetingId}/...` (topics, blocks, text blocks)
  and subscribe to `/topic/meetings/{meetingId}` to receive the resulting
  events. Subscribing to a meeting also returns its full current state.
- PostgreSQL for persistence, with Flyway migrations in
  `src/main/resources/db/migration`.
- Layering is explicit and consistent: controllers/websockets → `*Service` →
  `*StorageGateway` → Spring Data repository, with separate DTO (transport),
  BDO (domain) and DAO (JPA entity) types per package.
- Spring Security guards both the REST API and the WebSocket channel; every
  operation is checked against the organisation the session is scoped to.
- Tests run against a real PostgreSQL through Testcontainers (Docker required),
  with JaCoCo coverage and PIT mutation testing configured.

Frontend (`frontend/`)
===

SvelteKit 2 / Svelte 5 application in TypeScript, built with Vite.

- Route groups mirror the access model: `(public)` for login and registration,
  `(unscoped)` for picking an organisation, `(scoped)` for everything inside an
  organisation and `(scoped)/(admin)` for organisation administration.
- `src/lib/<domain>/` holds the API clients, WebSocket clients and views per
  domain (meeting, topic, block, textblock, organisation, user, session).
- `@stomp/stompjs` for the live meeting connection, `sveltekit-i18n` for
  translations.

Data model
==

```
organisation
 ├── users (role: ADMIN | MEMBER)
 └── meetings
      └── topics (name, description, optional duration)
           └── blocks (ordered; type TEXT)
                └── content
```

Running locally
==

Prerequisites: JDK 25, Node.js, Docker (for PostgreSQL, nginx and the backend
integration tests).

1. Start PostgreSQL and create a `notula` database. The defaults the backend
   expects are in `backend/src/main/resources/application.properties`
   (`localhost:5432`, user and password `postgres`). Flyway creates the schema
   on startup.
2. Backend (listens on port 7000):
   ```
   cd backend
   ./mvnw spring-boot:run
   ```
   Tests: `./mvnw verify`
3. Frontend (Vite dev server on port 5173):
   ```
   cd frontend
   npm install
   npm run dev
   ```
   Tests: `npm test` — formatting: `npm run format` / `npm run lint`
4. Start the nginx container described under [Setup](#setup) and open
   <https://localhost:4443>.

The frontend talks to `https://localhost:4443/api` and
`wss://localhost:4443/ws` (see `frontend/.env`), so nginx needs to be running
even for local development — that is what the certificate setup below is for.

Setup
==

The first steps have already been done, and you can skip to Adding the authority to the browser, and using it in nginx.
If you ever need to do the entire setup yourself, this are the steps:

Create local Certificate Authority:
```
openssl genrsa -out localCA.key 4096
openssl req -x509 -new -nodes -key localCA.key \
  -sha256 -days 3650 \
  -subj "/CN=Local Dev CA" \
  -out localCA.pem
```

Trust the Certificate Authority:
```
sudo cp localCA.pem /usr/local/share/ca-certificates/local-dev-ca.crt
sudo update-ca-certificates
```

Generate keys:
```
openssl genrsa -out localhost.key 2048

openssl req -new -key localhost.key \
  -out localhost.csr \
  -config localhost.cnf

openssl x509 -req \
  -in localhost.csr \
  -CA localCA.pem \
  -CAkey localCA.key \
  -CAcreateserial \
  -out localhost.crt \
  -days 825 \
  -sha256 \
  -extensions req_ext \
  -extfile localhost.cnf
```

Add authority to browser (Firefox):
```
Settings → Privacy & Security
Certificates → View Certificates
Authorities → Import → localCA.pem
```

Using in nginx:
```
docker run --name notula-nginx \
	-v ./nginx.conf:/etc/nginx/nginx.conf:ro \
	-v ./ssl:/etc/nginx/certs:ro \
	-p 4443:443 \
	-d nginx
```

Note: currently you still may have to update the ip addres the host.docker.internal binds to.
Note: on Linux, add the host mapping: `--add-host=host.docker.internal:host-gateway`
