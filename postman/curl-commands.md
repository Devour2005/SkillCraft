# SkillCraft API — curl commands for Postman

Plain, standalone curl commands — no shell variables, no `$(...)` substitution,
no piping into `grep`/`cut`. Each command is self-contained so it can be pasted
one at a time into Postman via *Import > Raw text* (or *New > Import* and
choosing the "raw text" tab), which turns a single curl command into one
request.

Replace the placeholder values (`<ANGLE_BRACKETS>`) with real ones — most
come from the JSON body of a previous response. All requests go through
`api-gateway` on `http://localhost:8081`, except section 7, which talks
directly to the two event-driven services for inspection.

Requires the stack to be running: `docker compose up -d`.

## 0. Setup

- `GATEWAY` = `http://localhost:8081`
- `NOTIFICATIONS` = `http://localhost:8084`
- `BILLING` = `http://localhost:8085`

(Postman users: define these as collection/environment variables and use
`{{GATEWAY}}` etc. instead of the literal URLs below.)

## 1. Student self-service: register, log in, browse courses

### 1.1 Register (public endpoint, always creates a STUDENT)

```
curl -X POST http://localhost:8081/auth/register \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"student1@skillcraft.com\",\"password\":\"password123\",\"firstName\":\"Dana\",\"lastName\":\"White\"}"
```

Response contains `accessToken` and `userId` — save both for the requests below
(`<STUDENT_TOKEN>` and `<STUDENT_USER_ID>`).

### 1.2 Log in explicitly

```
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"student1@skillcraft.com\",\"password\":\"password123\"}"
```

### 1.3 Browse active courses (any authenticated role)

```
curl http://localhost:8081/api/courses \
  -H "Authorization: Bearer <STUDENT_TOKEN>"
```

### 1.4 A student is not staff — must come back 403

```
curl -i "http://localhost:8081/api/users?role=STUDENT" \
  -H "Authorization: Bearer <STUDENT_TOKEN>"
```

## 2. Admin bootstrap (one manual DB step, by design)

Public registration only ever creates STUDENT accounts, so the very first
admin has to be promoted by hand. Everything after this is a normal admin
session.

### 2.1 Register the future admin like any other student

```
curl -X POST http://localhost:8081/auth/register \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"admin1@skillcraft.com\",\"password\":\"admin12345\",\"firstName\":\"Site\",\"lastName\":\"Admin\"}"
```

Save `userId` as `<ADMIN_USER_ID>`.

### 2.2 Manual, one-time: promote to ADMIN directly in Postgres

Run this in a terminal, not in Postman — it isn't an HTTP call:

```
docker compose exec -T postgres psql -U skillcraft -d skill_craft -c "UPDATE users SET role='ADMIN' WHERE id=<ADMIN_USER_ID>;"
```

(Use whatever value `POSTGRES_USER` has in your `.env` instead of `skillcraft`.)

### 2.3 Log in — the token now carries the ADMIN role

```
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"admin1@skillcraft.com\",\"password\":\"admin12345\"}"
```

Save `accessToken` as `<ADMIN_TOKEN>`.

## 3. Admin creates staff (teacher, manager, accountant)

### 3.1 Create a teacher

```
curl -X POST http://localhost:8081/api/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <ADMIN_TOKEN>" \
  -d "{\"email\":\"teacher1@skillcraft.com\",\"password\":\"password123\",\"firstName\":\"Tom\",\"lastName\":\"Reed\",\"role\":\"TEACHER\"}"
```

Save `id` as `<TEACHER_ID>`.

### 3.2 Create a manager

```
curl -X POST http://localhost:8081/api/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <ADMIN_TOKEN>" \
  -d "{\"email\":\"manager1@skillcraft.com\",\"password\":\"password123\",\"firstName\":\"Maria\",\"lastName\":\"Lopez\",\"role\":\"MANAGER\"}"
```

### 3.3 Create an accountant

```
curl -X POST http://localhost:8081/api/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <ADMIN_TOKEN>" \
  -d "{\"email\":\"accountant1@skillcraft.com\",\"password\":\"password123\",\"firstName\":\"Olga\",\"lastName\":\"Popova\",\"role\":\"ACCOUNTANT\"}"
```

Save `email` as `<ACCOUNTANT_EMAIL>` (used to log in later in section 6).

### 3.4 List users by role

```
curl "http://localhost:8081/api/users?role=TEACHER" \
  -H "Authorization: Bearer <ADMIN_TOKEN>"
```

```
curl "http://localhost:8081/api/users?role=MANAGER&role=ACCOUNTANT&role=ADMIN" \
  -H "Authorization: Bearer <ADMIN_TOKEN>"
```

### 3.5 Edit a user

```
curl -X PUT http://localhost:8081/api/users/<TEACHER_ID> \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <ADMIN_TOKEN>" \
  -d "{\"firstName\":\"Tom\",\"lastName\":\"Reed\",\"phone\":\"+1-555-0100\",\"isActive\":true}"
```

### 3.6 Deactivate a user

```
curl -X POST http://localhost:8081/api/users/<TEACHER_ID>/deactivate \
  -H "Authorization: Bearer <ADMIN_TOKEN>"
```

Sets `isActive` to `false`. Works for any role, including MANAGER — only
deletion is restricted for managers, not deactivation.

### 3.7 Delete a user

```
curl -i -X DELETE http://localhost:8081/api/users/<ACCOUNTANT_ID> \
  -H "Authorization: Bearer <ADMIN_TOKEN>"
```

Expect `204 No Content`. A follow-up `GET /api/users/<ACCOUNTANT_ID>` should
now return `404`.

### 3.8 Deleting a MANAGER is rejected

```
curl -i -X DELETE http://localhost:8081/api/users/<MANAGER_ID> \
  -H "Authorization: Bearer <ADMIN_TOKEN>"
```

Expect `400 Bad Request`:
`{"message":"Deleting a user with the MANAGER role is not allowed"}`

### 3.9 Deleting a user with dependent records is rejected too

```
curl -i -X DELETE http://localhost:8081/api/users/<TEACHER_ID> \
  -H "Authorization: Bearer <ADMIN_TOKEN>"
```

If `<TEACHER_ID>` still has courses assigned (or any other user still has
payments on record), expect `409 Conflict`:
`{"message":"Cannot complete this action: the record is still referenced by other data (e.g. courses, payments or enrollments)"}`

## 4. Courses

### 4.1 Create a course

```
curl -X POST http://localhost:8081/api/courses \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <ADMIN_TOKEN>" \
  -d "{\"title\":\"Algebra 101\",\"description\":\"Intro to algebra\",\"teacherId\":<TEACHER_ID>,\"price\":150.00}"
```

Save `id` as `<COURSE_ID>`.

### 4.2 List courses (paginated, active only)

```
curl "http://localhost:8081/api/courses?page=0&size=10" \
  -H "Authorization: Bearer <STUDENT_TOKEN>"
```

### 4.3 List all courses, including archived (staff only)

```
curl http://localhost:8081/api/courses/all \
  -H "Authorization: Bearer <ADMIN_TOKEN>"
```

### 4.4 Get a single course

```
curl http://localhost:8081/api/courses/<COURSE_ID> \
  -H "Authorization: Bearer <ADMIN_TOKEN>"
```

### 4.5 Update a course

```
curl -X PUT http://localhost:8081/api/courses/<COURSE_ID> \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <ADMIN_TOKEN>" \
  -d "{\"title\":\"Algebra 101\",\"description\":\"Updated syllabus\",\"teacherId\":<TEACHER_ID>,\"price\":175.00,\"isArchived\":false}"
```

## 5. Enrollment

Enrollment is keyed by the student's **user id** (the same id everything else
uses), not by any internal profile id.

### 5.1 Enroll a student in a course

```
curl -X POST "http://localhost:8081/api/enrollments?studentUserId=<STUDENT_USER_ID>&courseId=<COURSE_ID>" \
  -H "Authorization: Bearer <ADMIN_TOKEN>"
```

### 5.2 List a student's enrollments

```
curl http://localhost:8081/api/enrollments/student/<STUDENT_USER_ID> \
  -H "Authorization: Bearer <ADMIN_TOKEN>"
```

### 5.3 Enrolling the same student in the same course twice — must be rejected

```
curl -i -X POST "http://localhost:8081/api/enrollments?studentUserId=<STUDENT_USER_ID>&courseId=<COURSE_ID>" \
  -H "Authorization: Bearer <ADMIN_TOKEN>"
```

## 6. Payments

### 6.1 Log in as the accountant

```
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"<ACCOUNTANT_EMAIL>\",\"password\":\"password123\"}"
```

Save `accessToken` as `<ACCOUNTANT_TOKEN>`.

### 6.2 Record a tuition payment

```
curl -X POST http://localhost:8081/api/payments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <ACCOUNTANT_TOKEN>" \
  -d "{\"targetUserId\":<STUDENT_USER_ID>,\"amount\":150.00,\"type\":\"STUDENT_TUITION\",\"comment\":\"Tuition for Algebra 101\"}"
```

### 6.3 Only accountants can process payments — an admin gets 403

```
curl -i -X POST http://localhost:8081/api/payments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <ADMIN_TOKEN>" \
  -d "{\"targetUserId\":<STUDENT_USER_ID>,\"amount\":50.00,\"type\":\"BONUS\",\"comment\":\"Should be rejected\"}"
```

## 7. Event-driven services (Kafka)

`management` and `api-gateway` publish events to Kafka after each write above;
`notification-service` (8084) and `billing-service` (8085) consume them
asynchronously. Both are debug-only right now — reachable directly, not
through the gateway, no auth. It's fire-and-forget, not part of the
request/response cycle, so give the consumers a second or two before checking.

### 7.1 Registration -> notification (`user.registered`)

After re-running 1.1, check what the notification service received:

```
curl http://localhost:8084/notifications/user/<STUDENT_USER_ID>
```

Expect a `USER_REGISTERED` notification with the welcome message.

### 7.2 Enrollment -> notification + invoice (`enrollment.created`)

After re-running 5.1, check both consumers:

```
curl http://localhost:8084/notifications/user/<STUDENT_USER_ID>
```

Expect an additional `ENROLLMENT_CREATED` notification.

```
curl http://localhost:8085/invoices/student/<STUDENT_USER_ID>
```

Expect a `PENDING` invoice for the course, amount = the course price.

### 7.3 Payment -> notification + invoice closed (`payment.processed`)

After re-running 6.2, check both consumers again:

```
curl http://localhost:8084/notifications/user/<STUDENT_USER_ID>
```

Expect an additional `PAYMENT_PROCESSED` notification.

```
curl http://localhost:8085/invoices/student/<STUDENT_USER_ID>
```

Expect the invoice from 7.2 to now be `PAID`, with `relatedPaymentId` set.

### 7.4 All notifications / all invoices (debug)

```
curl http://localhost:8084/notifications
```

```
curl http://localhost:8085/invoices
```
