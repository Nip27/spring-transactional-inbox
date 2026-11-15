package io.github.inbox.notification.support;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Arrays;
import java.util.List;

/**
 * One SMTP server for the whole test JVM, standing in for the mail server outside the service.
 */
public final class MailServer {

    public static final GreenMail GREEN_MAIL = new GreenMail(ServerSetupTest.SMTP.dynamicPort());

    private MailServer() {
    }

    public static synchronized void start() {
        if (!GREEN_MAIL.isRunning()) {
            GREEN_MAIL.start();
        }
    }

    public static int port() {
        return GREEN_MAIL.getSmtp().getPort();
    }

    /**
     * The messages received for {@code recipient} so far; the inbox is shared by every test, so it is filtered.
     */
    public static List<MimeMessage> receivedBy(String recipient) {
        return Arrays.stream(GREEN_MAIL.getReceivedMessages())
                .filter(message -> isAddressedTo(message, recipient))
                .toList();
    }

    private static boolean isAddressedTo(Message message, String recipient) {
        try {
            return Arrays.stream(message.getAllRecipients())
                    .anyMatch(address -> ((InternetAddress) address).getAddress().equals(recipient));
        } catch (MessagingException e) {
            throw new IllegalStateException(e);
        }
    }
}
