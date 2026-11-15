--liquibase formatted sql

--changeset notification-service:002-notifications-event-id-unique
CREATE UNIQUE INDEX uq_notifications_event_id ON notifications (event_id);
