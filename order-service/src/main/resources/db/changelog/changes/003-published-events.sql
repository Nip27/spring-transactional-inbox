--liquibase formatted sql

--changeset order-service:003-published-events
CREATE TABLE published_events (
    id            UUID         PRIMARY KEY,
    event_type    VARCHAR(64)  NOT NULL,
    payload_type  VARCHAR(255) NOT NULL,
    payload       TEXT         NOT NULL,
    published_at  TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_published_events_type_published_at ON published_events (event_type, published_at);
