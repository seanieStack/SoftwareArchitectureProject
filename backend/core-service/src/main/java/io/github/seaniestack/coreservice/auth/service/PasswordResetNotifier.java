package io.github.seaniestack.coreservice.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PasswordResetNotifier {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${app.password-reset.frontend-base-url}")
    private String frontendBaseUrl;

    /** Used as From when sending mail; falls back to spring.mail.username if unset. */
    @Value("${app.password-reset.mail-from:}")
    private String configuredFrom;

    @Value("${spring.mail.username:}")
    private String springMailUsername;

    public void sendResetEmail(String email, String rawToken) {
        String base = frontendBaseUrl.endsWith("/")
                ? frontendBaseUrl.substring(0, frontendBaseUrl.length() - 1)
                : frontendBaseUrl;
        String resetUrl = base + "/reset-password?token=" + rawToken;

        JavaMailSender sender = mailSenderProvider.getIfAvailable();
        String from = !configuredFrom.isBlank() ? configuredFrom : springMailUsername;

        if (sender != null && from != null && !from.isBlank()) {
            try {
                SimpleMailMessage msg = new SimpleMailMessage();
                msg.setFrom(from);
                msg.setTo(email);
                msg.setSubject("Reset your E-Library password");
                msg.setText(
                        "You requested a password reset. Open this link to choose a new password (it expires in one hour):\n\n"
                                + resetUrl
                                + "\n\nIf you did not request this, you can ignore this email.");
                sender.send(msg);
                log.info("Password reset email sent to {}", email);
                return;
            } catch (Exception e) {
                log.error("Failed to send password reset email to {}", email, e);
            }
        }

        log.info("Password reset link for {}: {}", email, resetUrl);
    }
}
