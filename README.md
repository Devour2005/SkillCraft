# SkillCraft

Internal accounting system for a course school: students, courses, teachers, enrollments, billing.

## Modules

- [`api-gateway`](api-gateway) — the single entry point. Login/registration, JWT issuing and
  validation, and proxying of everything else to `management`. Also serves the web UI (login,
  registration, admin panel).
- [`management`](management) — all business logic: users, courses, enrollments, payments.
  Has no login of its own — it only trusts a valid JWT signed with the same secret as
  `api-gateway`. Publishes domain events to Kafka (`user.registered`, `enrollment.created`,
  `payment.processed`) after each corresponding write.
- [`notification-service`](notification-service) — consumes all three event topics and "sends"
  a notification: there's no real email/SMS provider wired up yet, so sending means logging it
  and writing a row to its own table. Not routed through `api-gateway` yet — reachable directly
  at http://localhost:8084 (`/notifications`, `/notifications/user/{userId}`) for inspection.
- [`billing-service`](billing-service) — consumes `enrollment.created` to issue a PENDING
  invoice and `payment.processed` (tuition only) to mark the oldest matching invoice PAID.
  PDF generation (real invoices/receipts) is deferred — issuing and receipting just means
  writing the row and logging what would have been produced. Also not yet routed through the
  gateway — reachable directly at http://localhost:8085 (`/invoices`, `/invoices/student/{userId}`).

All four services share the same PostgreSQL instance. `management` owns the `public` schema
(migrations live there); `notification-service` and `billing-service` each get their own schema
(`notification`, `billing`) so their independent Flyway histories don't collide with
`management`'s or each other's.

Events are plain JSON strings on the wire (no Kafka type headers) — each consumer keeps its own
local copy of the event record and parses the payload itself, so producers and consumers never
need to share a Java package.

## Running locally

```bash
cp .env.example .env   # fill in real values; .env is not committed
docker compose up --build
```

- `api-gateway` — http://localhost:8081 (`/login`, `/register`, `/admin`, JSON API at `/auth/**` and `/api/**`)
- `management` — http://localhost:8080 (only meant to be reached directly for debugging; should be
  closed off from external traffic in production)
- `notification-service` — http://localhost:8084 (debug-only, no auth)
- `billing-service` — http://localhost:8085 (debug-only, no auth)
- `kafka` — localhost:9092 (single-node KRaft broker, no Zookeeper)

## Build

```bash
./gradlew build
```

## Testing

`./gradlew test` runs the full suite. Each module that touches Postgres and/or Kafka has an
integration test backed by [Testcontainers](https://testcontainers.com/) (a real, disposable
Postgres and/or Kafka broker per test run, not a mock):

- `management` — `DomainEventPublishingIT`: creating a user/enrollment/payment through the real
  service layer actually lands the corresponding event on the real Kafka topic.
- `api-gateway` — `AuthServiceIT`: self-registration writes `users`/`students` correctly and
  publishes `user.registered`; wrong password is rejected.
- `notification-service` — `NotificationEventListenerIT`: a raw JSON message on each topic ends
  up as a `Notification` row (email resolved via a seeded `users` row, since this service reads
  management's table but doesn't own it).
- `billing-service` — `BillingEventListenerIT`: `enrollment.created` issues a PENDING invoice,
  a matching `payment.processed` closes it.

Needs Docker available to the JVM running the tests, same as any Testcontainers setup. If you
hit `advertised.listeners cannot use the nonroutable meta-address 0.0.0.0` from the Kafka
container, pin `apache/kafka:4.1.0` or newer in `TestcontainersConfiguration` — this repo was
initially wired against `3.9.0` and hit a real incompatibility between that tag and
`testcontainers-kafka:2.0.5`, fixed by moving to the newer image.

Postman/curl scripts for exercising the whole thing through a running `docker compose` stack
(including these Kafka side effects) live in [`postman/`](postman).
