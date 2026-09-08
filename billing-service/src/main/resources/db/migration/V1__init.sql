CREATE TABLE invoices
(
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    enrollment_id       BIGINT,
    student_user_id     BIGINT         NOT NULL,
    course_id           BIGINT,
    course_title        VARCHAR(200)   NOT NULL,
    amount              NUMERIC(10, 2) NOT NULL,
    status              VARCHAR(20)    NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING', 'PAID', 'CANCELLED')),
    related_payment_id  BIGINT,
    created_at          TIMESTAMPTZ    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    paid_at             TIMESTAMPTZ
);

CREATE INDEX idx_invoices_student_user_id ON invoices (student_user_id);
CREATE INDEX idx_invoices_status ON invoices (status);
