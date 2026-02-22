package com.maja.med_app.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
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


    @Async
    public void sendDoctorAccountEmail(String toEmail, String firstName, String tempPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        
        message.setFrom("tst45555@gmail.com"); 
        message.setTo(toEmail);
        message.setSubject("Your Doctor Account has been created!");
        
        message.setText("Hello Dr. " + firstName + ",\n\n" +
                "An account has been created for you in the MedApp Clinic system by the Administrator.\n\n" +
                "Here are your login details:\n" +
                "Email (Login): " + toEmail + "\n" +
                "Temporary Password: " + tempPassword + "\n\n" +
                "⚠️ Security Notice: Please log in and change your password immediately in the 'My Profile' tab.\n\n" +
                "Best regards,\n" +
                "MedApp Clinic Administration");

        mailSender.send(message);
    }
}
