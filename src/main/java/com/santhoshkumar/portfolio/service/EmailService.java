package com.santhoshkumar.portfolio.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // 🔹 ADMIN MAIL
    public void sendAdminMail(
            String to,
            String subject,
            String name,
            String email,
            String messageText
    ) {

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(buildAdminEmail(name, email, messageText), true);

            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send admin email", e);
        }
    }

    // 🔹 AUTO REPLY MAIL
    public void sendAutoReply(String userEmail, String userName) {

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(userEmail);
            helper.setSubject("Thanks for contacting me 👋");
            helper.setText(buildAutoReplyEmail(userName), true);

            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send auto reply", e);
        }
    }

    // ================= HTML TEMPLATES =================

    private String buildAdminEmail(String name, String email, String message) {
        return """
        <html>
        <body style="font-family:Arial;background:#f4f6f8;margin:0;padding:0;">
          <div style="max-width:600px;margin:20px auto;background:#fff;
                      padding:16px;border-radius:10px;
                      box-shadow:0 6px 18px rgba(0,0,0,.08);">
            <h3>📩 New Contact Message</h3>
            <p><strong>Name:</strong> %s</p>
            <p><strong>Email:</strong> %s</p>
            <p><strong>Message:</strong></p>
            <div style="background:#f8f9fa;padding:10px;border-radius:6px;">
              %s
            </div>
            <p style="font-size:11px;color:#777;margin-top:10px;">
              © %d Portfolio
            </p>
          </div>
        </body>
        </html>
        """.formatted(
                name,
                email,
                message.replace("\n", "<br/>"),
                java.time.Year.now().getValue()
        );
    }

    private String buildAutoReplyEmail(String name) {
        return """
        <html>
        <body style="font-family:Arial;background:#f4f6f8;margin:0;padding:0;">
          <div style="max-width:600px;margin:20px auto;background:#ffffff;
                      padding:18px;border-radius:10px;
                      box-shadow:0 6px 18px rgba(0,0,0,.08);">

            <h2 style="margin:0 0 10px;">Hello %s 👋</h2>

            <p style="color:#444;">
              Thank you for reaching out through my portfolio.
            </p>

            <p style="color:#444;">
              I’ve received your message and will get back to you
              as soon as possible.
            </p>

            <p style="color:#444;">
              Looking forward to connecting with you!
            </p>

            <br/>

            <p style="margin:0;font-weight:bold;">
              Regards,<br/>
              Santhoshkumar Raman
            </p>

            <p style="font-size:12px;color:#777;margin-top:8px;">
              Software Developer | Angular | Spring Boot
            </p>

          </div>
        </body>
        </html>
        """.formatted(name);
    }
}
