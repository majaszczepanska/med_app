package com.maja.med_app.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendRegistrationEmail(String toEmail, String firstName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("tst45555@gmail.com");
        message.setTo(toEmail);
        message.setSubject("Welcome to MedApp!");
        message.setText("Dear " + firstName + ",\n\nThank you for registering at MedApp. We're excited to have you on board!\n\nBest regards,\nMedApp Team");
        mailSender.send(message);
    }
}
