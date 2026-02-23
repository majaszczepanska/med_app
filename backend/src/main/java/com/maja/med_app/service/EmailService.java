package com.maja.med_app.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendRegistrationEmail(String toEmail, String firstName, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            String verificationLink = "http://192.168.131.213:8080/auth/verify?token=" + token;
            helper.setFrom("tst45555@gmail.com");
            helper.setTo(toEmail);
            helper.setSubject("Please verify your MedApp account!");
            String htmlContent = "<div style='font-family: Arial, sans-serif; color: #333;'>"
                        + "<h3>Hello " + firstName + ",</h3>"
                        + "<p>Thank you for registering at MedApp Clinic! To activate your account, please click the button below:</p>"
                        + "<p style='margin: 25px 0;'>"
                        + "<a href=\"" + verificationLink + "\" style=\"background-color: #6f42c1; color: white; padding: 12px 24px; text-decoration: none; border-radius: 6px; font-weight: bold; display: inline-block;\">ACTIVATE ACCOUNT</a>"
                        + "</p>"
                        + "<p style='font-size: 0.9em; color: #666;'>Or copy this link to your browser:<br>"
                        + "<a href=\"" + verificationLink + "\">" + verificationLink + "</a></p>"
                        + "<br><p>Best regards,<br><strong>MedApp Clinic Team</strong></p>"
                        + "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
            System.out.println("✅ Verification HTML email sent to: " + toEmail);
        } catch (Exception e) {
            System.err.println("❌ Failed to send verification email: " + e.getMessage());
        }
        
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


    @Async
    public void sendAppointmentConfirmation(String toEmail, String patientName, String doctorName, String visitDate) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8"); 
            
            helper.setFrom("tst45555@gmail.com");
            helper.setTo(toEmail);
            helper.setSubject("Appointment Confirmed!");

            String htmlContent = "<div style='font-family: Arial, sans-serif; color: #333; padding: 20px;'>"
                    + "<h2 style='color: #6f42c1;'>Appointment Confirmed!</h2>"
                    + "<p>Hello <strong>" + patientName + "</strong>,</p>"
                    + "<p>Your appointment at MedApp Clinic has been successfully booked.</p>"
                    + "<div style='background-color: #f8f9fa; padding: 15px; border-left: 5px solid #6f42c1; margin: 20px 0;'>"
                    + "  <p style='margin: 5px 0;'>👨‍⚕️ <strong>Doctor:</strong> Dr. " + doctorName + "</p>"
                    + "  <p style='margin: 5px 0;'>🕒 <strong>Date & Time:</strong> " + visitDate.replace("T", " ") + "</p>"
                    + "</div>"
                    + "<p>If you need to cancel or reschedule, please log in to your Patient Portal.</p>"
                    + "<p>See you soon!<br><strong>MedApp Clinic Team</strong></p>"
                    + "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
            System.out.println("✅ Appointment confirmation email sent to: " + toEmail);
            
        } catch (Exception e) {
            System.err.println("❌ Failed to send appointment email: " + e.getMessage());
        }
    }
}
