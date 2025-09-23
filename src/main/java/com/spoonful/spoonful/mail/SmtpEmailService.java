package com.spoonful.spoonful.mail;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class SmtpEmailService implements EmailService {
    private final JavaMailSender sender;

    public SmtpEmailService(JavaMailSender sender) { this.sender = sender; }

    @Override
    public void send(String to, String subject, String htmlBody) {
        try {
            var msg = sender.createMimeMessage();
            var helper = new MimeMessageHelper(msg, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            sender.send(msg);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email to " + to, e);
        }
    }
}