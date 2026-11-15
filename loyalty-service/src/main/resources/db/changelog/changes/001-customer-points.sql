--liquibase formatted sql

--changeset loyalty-service:001-customer-points
CREATE TABLE customer_points (
    customer_id  BIGINT       PRIMARY KEY,
    balance      BIGINT       NOT NULL,
    updated_at   TIMESTAMPTZ  NOT NULL,
    version      BIGINT       NOT NULL
);
