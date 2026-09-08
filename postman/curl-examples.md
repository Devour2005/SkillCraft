# SkillCraft API — curl scenarios

Same scenarios as `SkillCraft.postman_collection.json`, as plain bash/curl chains for
terminal use (or to paste one-by-one into Postman via *Import > Raw text*). Every command
talks to `api-gateway` on `http://localhost:8081` — that's the only port a client is
meant to call; `management` (8080) is internal.

Requires the stack to be running: `docker compose up -d`.

## 0. Setup

```bash
GATEWAY=http://localhost:8081
```

## 1. Student self-service: register, log in, browse courses

```bash
# Register (public endpoint, always creates a STUDENT)
STUDENT_EMAIL="student.$(date +%s)@skillcraft.com"
STUDENT_RESPONSE=$(curl -s -X POST "$GATEWAY/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$STUDENT_EMAIL\",\"password\":\"password123\",\"firstName\":\"Dana\",\"lastName\":\"White\"}")
echo "$STUDENT_RESPONSE"

STUDENT_TOKEN=$(echo "$STUDENT_RESPONSE" | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)
STUDENT_USER_ID=$(echo "$STUDENT_RESPONSE" | grep -o '"userId":[0-9]*' | grep -o '[0-9]*')


curl -X POST http://localhost:8081/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@skillcraft.com",
    "password": "password123",
    "firstName": "Имя",
    "lastName": "Фамилия"
  }'

# Log in again explicitly (same result, just via /auth/login instead of the register response)
curl -s -X POST "$GATEWAY/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$STUDENT_EMAIL\",\"password\":\"password123\"}"

# Browse active courses - any authenticated role can do this
curl -s "$GATEWAY/api/courses" -H "Authorization: Bearer $STUDENT_TOKEN"

# A student is not staff - this must come back 403
curl -s -o /dev/null -w "HTTP %{http_code}\n" \
  "$GATEWAY/api/users?role=STUDENT" -H "Authorization: Bearer $STUDENT_TOKEN"
```

## 2. Admin bootstrap (one manual DB step, by design)

Public registration only ever creates STUDENT accounts, so the very first admin has to be
promoted by hand. Everything after this is a normal admin session.

```bash
ADMIN_EMAIL="admin.$(date +%s)@skillcraft.com"
ADMIN_RESPONSE=$(curl -s -X POST "$GATEWAY/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$ADMIN_EMAIL\",\"password\":\"admin12345\",\"firstName\":\"Site\",\"lastName\":\"Admin\"}")
ADMIN_USER_ID=$(echo "$ADMIN_RESPONSE" | grep -o '"userId":[0-9]*' | grep -o '[0-9]*')

# Manual, one-time: promote to ADMIN directly in Postgres
docker compose exec -T postgres psql -U "$(grep POSTGRES_USER .env | cut -d= -f2)" -d skill_craft \
  -c "UPDATE users SET role='ADMIN' WHERE id=$ADMIN_USER_ID;"

# Now log in - the token will carry the ADMIN role
ADMIN_TOKEN=$(curl -s -X POST "$GATEWAY/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$ADMIN_EMAIL\",\"password\":\"admin12345\"}" \
  | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)
```

## 3. Admin creates staff (teacher, manager, accountant)

```bash
TEACHER_RESPONSE=$(curl -s -X POST "$GATEWAY/api/users" \
  -H "Content-Type: application/json" -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d "{\"email\":\"teacher.$(date +%s)@skillcraft.com\",\"password\":\"password123\",\"firstName\":\"Tom\",\"lastName\":\"Reed\",\"role\":\"TEACHER\"}")
TEACHER_ID=$(echo "$TEACHER_RESPONSE" | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*')

curl -s -X POST "$GATEWAY/api/users" \
  -H "Content-Type: application/json" -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d "{\"email\":\"manager.$(date +%s)@skillcraft.com\",\"password\":\"password123\",\"firstName\":\"Maria\",\"lastName\":\"Lopez\",\"role\":\"MANAGER\"}"

ACCOUNTANT_RESPONSE=$(curl -s -X POST "$GATEWAY/api/users" \
  -H "Content-Type: application/json" -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d "{\"email\":\"accountant.$(date +%s)@skillcraft.com\",\"password\":\"password123\",\"firstName\":\"Olga\",\"lastName\":\"Popova\",\"role\":\"ACCOUNTANT\"}")
ACCOUNTANT_EMAIL=$(echo "$ACCOUNTANT_RESPONSE" | grep -o '"email":"[^"]*"' | cut -d'"' -f4)

# List by role (repeat the query param for several roles at once)
curl -s "$GATEWAY/api/users?role=TEACHER" -H "Authorization: Bearer $ADMIN_TOKEN"
curl -s "$GATEWAY/api/users?role=MANAGER&role=ACCOUNTANT&role=ADMIN" -H "Authorization: Bearer $ADMIN_TOKEN"

# Edit a user
curl -s -X PUT "$GATEWAY/api/users/$TEACHER_ID" \
  -H "Content-Type: application/json" -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{"firstName":"Tom","lastName":"Reed","phone":"+1-555-0100","isActive":true}'
```

## 4. Courses

```bash
COURSE_RESPONSE=$(curl -s -X POST "$GATEWAY/api/courses" \
  -H "Content-Type: application/json" -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d "{\"title\":\"Algebra 101\",\"description\":\"Intro to algebra\",\"teacherId\":$TEACHER_ID,\"price\":150.00}")
COURSE_ID=$(echo "$COURSE_RESPONSE" | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*')

curl -s "$GATEWAY/api/courses?page=0&size=10" -H "Authorization: Bearer $STUDENT_TOKEN"
curl -s "$GATEWAY/api/courses/all" -H "Authorization: Bearer $ADMIN_TOKEN"
curl -s "$GATEWAY/api/courses/$COURSE_ID" -H "Authorization: Bearer $ADMIN_TOKEN"

curl -s -X PUT "$GATEWAY/api/courses/$COURSE_ID" \
  -H "Content-Type: application/json" -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d "{\"title\":\"Algebra 101\",\"description\":\"Updated syllabus\",\"teacherId\":$TEACHER_ID,\"price\":175.00,\"isArchived\":false}"
```

## 5. Enrollment

Enrollment is keyed by the student's **user id** (the same id everything else uses), not
by any internal profile id.

```bash
curl -s -X POST "$GATEWAY/api/enrollments?studentUserId=$STUDENT_USER_ID&courseId=$COURSE_ID" \
  -H "Authorization: Bearer $ADMIN_TOKEN"

curl -s "$GATEWAY/api/enrollments/student/$STUDENT_USER_ID" -H "Authorization: Bearer $ADMIN_TOKEN"

# Enrolling the same student in the same course twice is rejected
curl -s -o /dev/null -w "HTTP %{http_code}\n" -X POST \
  "$GATEWAY/api/enrollments?studentUserId=$STUDENT_USER_ID&courseId=$COURSE_ID" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

## 6. Payments

```bash
ACCOUNTANT_TOKEN=$(curl -s -X POST "$GATEWAY/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$ACCOUNTANT_EMAIL\",\"password\":\"password123\"}" \
  | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)

curl -s -X POST "$GATEWAY/api/payments" \
  -H "Content-Type: application/json" -H "Authorization: Bearer $ACCOUNTANT_TOKEN" \
  -d "{\"targetUserId\":$STUDENT_USER_ID,\"amount\":150.00,\"type\":\"STUDENT_TUITION\",\"comment\":\"Tuition for Algebra 101\"}"

# Only accountants can process payments - an admin gets 403
curl -s -o /dev/null -w "HTTP %{http_code}\n" -X POST "$GATEWAY/api/payments" \
  -H "Content-Type: application/json" -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d "{\"targetUserId\":$STUDENT_USER_ID,\"amount\":50.00,\"type\":\"BONUS\",\"comment\":\"Should be rejected\"}"
```

## 7. Event-driven services (Kafka)

`management` and `api-gateway` publish events to Kafka after each write below;
`notification-service` (8084) and `billing-service` (8085) consume them asynchronously.
Both are debug-only right now - reachable directly, not through the gateway, no auth.
Give the consumers a second or two to catch up before checking - it's fire-and-forget,
not part of the request/response cycle.

```bash
NOTIFICATIONS=http://localhost:8084
BILLING=http://localhost:8085
```

### 7.1 Registration -> notification (`user.registered`)

Re-run section 1's registration, then check what the notification service received:

```bash
sleep 2
curl -s "$NOTIFICATIONS/notifications/user/$STUDENT_USER_ID"
# -> a USER_REGISTERED notification with the welcome message
```

### 7.2 Enrollment -> notification + invoice (`enrollment.created`)

Re-run section 5's enrollment, then check both consumers:

```bash
sleep 2
curl -s "$NOTIFICATIONS/notifications/user/$STUDENT_USER_ID"
# -> also has an ENROLLMENT_CREATED notification now

curl -s "$BILLING/invoices/student/$STUDENT_USER_ID"
# -> a PENDING invoice for the course, amount = the course price
```

### 7.3 Payment -> notification + invoice closed (`payment.processed`)

Re-run section 6's tuition payment, then:

```bash
sleep 2
curl -s "$NOTIFICATIONS/notifications/user/$STUDENT_USER_ID"
# -> also has a PAYMENT_PROCESSED notification now

curl -s "$BILLING/invoices/student/$STUDENT_USER_ID"
# -> the invoice from 7.2 is now PAID, relatedPaymentId is set
```

### 7.4 All notifications / all invoices (debug)

```bash
curl -s "$NOTIFICATIONS/notifications"
curl -s "$BILLING/invoices"
```

### 7.5 Watch the consumers work in real time

```bash
docker compose logs -f notification-service billing-service
```

