package io.github.inbox.notification.mail;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.context.TestConstructor;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = MailSenderAutoConfiguration.class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class MailSenderTimeoutsIntegrationTest {

    private final JavaMailSender mailSender;

    MailSenderTimeoutsIntegrationTest(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Test
    @DisplayName("IT mail sender when the service starts should bound connecting, reading and writing")
    void mailSender_whenServiceStarts_shouldBoundEveryWait() {
        // when / then
        assertThat(((JavaMailSenderImpl) mailSender).getJavaMailProperties())
                .containsEntry("mail.smtp.connectiontimeout", "5000")
                .containsEntry("mail.smtp.timeout", "10000")
                .containsEntry("mail.smtp.writetimeout", "10000");
    }
}
