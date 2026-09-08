CREATE TABLE notifications
(
    id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    recipient_user_id  BIGINT,
    recipient_email    VARCHAR(255) NOT NULL,
    event_type         VARCHAR(50)  NOT NULL,
    subject            VARCHAR(255) NOT NULL,
    body               TEXT         NOT NULL,
    sent_at            TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notifications_recipient_user_id ON notifications (recipient_user_id);
