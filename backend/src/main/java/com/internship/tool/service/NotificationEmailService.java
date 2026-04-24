package com.internship.tool.service;

import com.internship.tool.entity.RegulatoryChange;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class NotificationEmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationEmailService.class);

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${notification.mail-from}")
    private String mailFrom;

    @Value("${notification.email-to}")
    private String notificationRecipients;

    public NotificationEmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    public void sendCreateNotification(RegulatoryChange regulatoryChange) {
        if (!hasRecipients()) {
            return;
        }

        Context context = new Context();
        context.setVariable("change", regulatoryChange);
        context.setVariable("generatedAt", LocalDateTime.now());

        String htmlBody = templateEngine.process("regulatory-change-created", context);
        String subject = "[RCM] New Regulatory Change Created - " + regulatoryChange.getTitle();
        sendHtmlEmail(subject, htmlBody);
    }

    public void sendOverdueNotification(List<RegulatoryChange> overdueChanges) {
        if (!hasRecipients() || overdueChanges == null || overdueChanges.isEmpty()) {
            return;
        }

        Context context = new Context();
        context.setVariable("changes", overdueChanges);
        context.setVariable("count", overdueChanges.size());
        context.setVariable("generatedAt", LocalDateTime.now());

        String htmlBody = templateEngine.process("regulatory-change-overdue", context);
        String subject = "[RCM] Overdue Regulatory Changes Alert (" + overdueChanges.size() + ")";
        sendHtmlEmail(subject, htmlBody);
    }

    private void sendHtmlEmail(String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(mailFrom);
            helper.setTo(parseRecipients());
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
        } catch (MessagingException | MailException exception) {
            LOGGER.warn("Failed to send email notification: {}", exception.getMessage());
        }
    }

    private String[] parseRecipients() {
        return Arrays.stream(notificationRecipients.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toArray(String[]::new);
    }

    private boolean hasRecipients() {
        return notificationRecipients != null && !notificationRecipients.trim().isBlank();
    }
}
