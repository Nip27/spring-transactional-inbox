package io.github.inbox.notification.mail;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestConstructor;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The mail sender as the service configures it, against a mail server that accepts the connection and never says
 * a word. The read timeout is shortened for the run; its production value is checked by
 * {@code MailSenderTimeoutsIntegrationTest}.
 */
@SpringBootTest(classes = MailSenderAutoConfiguration.class, properties = "SMTP_READ_TIMEOUT_MS=500")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class SilentMailServerIntegrationTest {

    private static final List<Socket> ACCEPTED = new CopyOnWriteArrayList<>();
    private static ServerSocket server;

    private final JavaMailSender mailSender;

    SilentMailServerIntegrationTest(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @BeforeAll
    static void startSilentServer() throws IOException {
        server = new ServerSocket(0, 1, InetAddress.getLoopbackAddress());
        Thread acceptor = new Thread(() -> {
            while (!server.isClosed()) {
                try {
                    ACCEPTED.add(server.accept());
                } catch (IOException e) {
                    return;
                }
            }
        }, "silent-smtp");
        acceptor.setDaemon(true);
        acceptor.start();
    }

    @AfterAll
    static void stopSilentServer() throws IOException {
        server.close();
        for (Socket socket : ACCEPTED) {
            socket.close();
        }
    }

    @DynamicPropertySource
    static void smtp(DynamicPropertyRegistry registry) {
        registry.add("spring.mail.host", () -> "127.0.0.1");
        registry.add("spring.mail.port", () -> server.getLocalPort());
    }

    @Test
    @Timeout(10)
    @DisplayName("IT send() when the mail server never answers should fail instead of waiting forever")
    void send_whenServerNeverAnswers_shouldFailWithinTimeout() {
        // given
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("orders@example.com");
        message.setTo("jane.doe@example.com");
        message.setSubject("Your order 7 is confirmed");
        message.setText("Thank you for your order 7.");

        // when / then
        assertThatThrownBy(() -> mailSender.send(message))
                .isInstanceOf(MailSendException.class)
                .hasStackTraceContaining("timed out");
    }
}
