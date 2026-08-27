CREATE TYPE user_role AS ENUM (
    'MANAGER',
    'ADMIN',
    'TEACHER',
    'STUDENT',
    'ACCOUNTANT'
    );

CREATE TYPE enrollment_status AS ENUM (
    'ACTIVE',
    'COMPLETED',
    'DROPPED'
    );

CREATE TYPE payment_type AS ENUM (
    'STUDENT_TUITION',
    'SALARY_PAYOUT',
    'BONUS',
    'REFUND'
    );

CREATE TYPE payment_status AS ENUM (
    'PENDING',
    'COMPLETED',
    'CANCELLED'
    );


CREATE TABLE users
(
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name    VARCHAR(100) NOT NULL,
    last_name     VARCHAR(100) NOT NULL,
    phone         VARCHAR(30),
    role          user_role    NOT NULL,
    is_active     BOOLEAN      NOT NULL DEFAULT true,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE managers
(
    id      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,

    CONSTRAINT fk_managers_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE staff_profiles
(
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id    BIGINT NOT NULL UNIQUE,
    manager_id BIGINT,
    hire_date  DATE,

    CONSTRAINT fk_staff_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_staff_manager FOREIGN KEY (manager_id)
        REFERENCES managers (id) ON DELETE SET NULL
);

CREATE TABLE students
(
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id    BIGINT NOT NULL UNIQUE,
    birth_date DATE,
    notes      TEXT,

    CONSTRAINT fk_students_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE courses
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title       VARCHAR(200)   NOT NULL,
    description TEXT,
    teacher_id  BIGINT         NOT NULL,
    price       NUMERIC(10, 2) NOT NULL CHECK (price >= 0),
    is_archived BOOLEAN        NOT NULL DEFAULT false,

    CONSTRAINT fk_courses_teacher FOREIGN KEY (teacher_id)
        REFERENCES users (id) ON DELETE RESTRICT
);

CREATE TABLE enrollments
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    student_id  BIGINT            NOT NULL,
    course_id   BIGINT            NOT NULL,
    enrolled_at TIMESTAMPTZ       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status      enrollment_status NOT NULL DEFAULT 'ACTIVE',

    CONSTRAINT fk_enrollments_student FOREIGN KEY (student_id)
        REFERENCES students (id) ON DELETE CASCADE,
    CONSTRAINT fk_enrollments_course FOREIGN KEY (course_id)
        REFERENCES courses (id) ON DELETE CASCADE,
    CONSTRAINT uq_student_course UNIQUE (student_id, course_id)
);

CREATE TABLE lessons
(
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    course_id  BIGINT       NOT NULL,
    title      VARCHAR(200) NOT NULL,
    start_time TIMESTAMPTZ  NOT NULL,
    end_time   TIMESTAMPTZ  NOT NULL,

    CONSTRAINT fk_lessons_course FOREIGN KEY (course_id)
        REFERENCES courses (id) ON DELETE CASCADE,
    CONSTRAINT chk_lesson_times CHECK (end_time > start_time)
);

CREATE TABLE payments
(
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    accountant_id  BIGINT         NOT NULL,
    target_user_id BIGINT         NOT NULL,
    amount         NUMERIC(10, 2) NOT NULL,
    type           payment_type   NOT NULL,
    status         payment_status NOT NULL DEFAULT 'COMPLETED',
    payment_date   TIMESTAMPTZ    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    comment        VARCHAR(500),

    CONSTRAINT fk_payments_accountant FOREIGN KEY (accountant_id)
        REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT fk_payments_target_user FOREIGN KEY (target_user_id)
        REFERENCES users (id) ON DELETE RESTRICT
);


CREATE INDEX idx_users_role ON users (role);
CREATE INDEX idx_users_email ON users (email);

CREATE INDEX idx_staff_profiles_manager_id ON staff_profiles (manager_id);
CREATE INDEX idx_courses_teacher_id ON courses (teacher_id);
CREATE INDEX idx_enrollments_student_id ON enrollments (student_id);
CREATE INDEX idx_enrollments_course_id ON enrollments (course_id);
CREATE INDEX idx_lessons_course_id ON lessons (course_id);

CREATE INDEX idx_lessons_start_time ON lessons (start_time);
CREATE INDEX idx_payments_target_user_id ON payments (target_user_id);
CREATE INDEX idx_payments_payment_date ON payments (payment_date);