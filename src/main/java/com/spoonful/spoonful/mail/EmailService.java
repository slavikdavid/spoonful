package com.spoonful.spoonful.mail;

public interface EmailService {
    void send(String to, String subject, String htmlBody);
}