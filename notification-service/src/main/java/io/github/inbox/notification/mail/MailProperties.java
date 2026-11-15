package io.github.inbox.notification.mail;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @param from the sender address of every e-mail the service sends
 */
@ConfigurationProperties(prefix = "notification.mail")
public record MailProperties(String from) {
}
