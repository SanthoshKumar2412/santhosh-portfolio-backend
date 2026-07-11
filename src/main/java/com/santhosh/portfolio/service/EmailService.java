package com.santhosh.portfolio.service;

import com.santhosh.portfolio.dto.ContactRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final String RESEND_API_URL = "https://api.resend.com/emails";

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.mail.owner-address}")
    private String ownerAddress;

    // Resend API key, e.g. re_xxxxxxxx
    @Value("${resend.api-key}")
    private String resendApiKey;

    // Must be on a domain verified in Resend, e.g. "Portfolio <contact@yourdomain.com>"
    // Until you verify a domain, use Resend's shared test sender: onboarding@resend.dev
    @Value("${resend.from-address}")
    private String fromAddress;

    /**
     * Sends the owner notification (styled HTML, quick scan in your inbox)
     * and the sender's auto-reply (styled HTML, on-brand) via Resend's HTTP
     * API. Runs on a background thread so the HTTP request doesn't wait.
     * Uses HTTPS (port 443) instead of raw SMTP, so it isn't affected by
     * Render's free-tier SMTP port block (25, 465, 587).
     */
    @Async
    public void sendContactEmails(ContactRequest request) {
        try {
            sendEmail(
                    ownerAddress,
                    "Portfolio contact form: " + request.getName(),
                    buildOwnerNotificationHtml(request),
                    request.getEmail()
            );

            sendEmail(
                    request.getEmail(),
                    "Thanks for reaching out, " + request.getName(),
                    buildAutoReplyHtml(request),
                    null
            );
        } catch (Exception ex) {
            log.error("Failed to send contact form emails for {}", request.getEmail(), ex);
        }
    }

    /**
     * Sends a single email via Resend's REST API.
     *
     * @param to      recipient address
     * @param subject email subject
     * @param html    email HTML body
     * @param replyTo optional reply-to address (used for the owner notification
     *                so replying goes straight to the form sender)
     */
    private void sendEmail(String to, String subject, String html, String replyTo) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(resendApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("from", fromAddress);
        body.put("to", List.of(to));
        body.put("subject", subject);
        body.put("html", html);
        if (replyTo != null && !replyTo.isBlank()) {
            body.put("reply_to", replyTo);
        }

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        restTemplate.postForEntity(RESEND_API_URL, entity, String.class);
    }

    private String buildOwnerNotificationHtml(ContactRequest request) {
        String safeName = escapeHtml(request.getName());
        String safeEmail = escapeHtml(request.getEmail());
        String safeMessage = escapeHtml(request.getMessage());
        String mailtoLink = "mailto:" + request.getEmail() + "?subject=" +
                java.net.URLEncoder.encode("Re: your message to Santhoshkumar Raman", java.nio.charset.StandardCharsets.UTF_8);

        return "<!DOCTYPE html>" +
            "<html><head>" +
            "<meta charset=\"UTF-8\">" +
            "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
            "<meta name=\"format-detection\" content=\"telephone=no\">" +
            "</head>" +
            "<body style=\"margin:0;padding:0;background-color:#0e1b26;font-family:Arial, Helvetica, sans-serif;\">" +
            "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" bgcolor=\"#0e1b26\" style=\"background-color:#0e1b26;padding:40px 0;\">" +
            "<tr><td align=\"center\">" +

            "<table role=\"presentation\" width=\"480\" cellpadding=\"0\" cellspacing=\"0\" bgcolor=\"#182329\" " +
            "style=\"background-color:#182329;border-radius:12px;border:1px solid rgba(255,255,255,0.08);overflow:hidden;\">" +

            // Header bar
            "<tr><td style=\"padding:28px 32px 0 32px;\">" +
            "<span style=\"font-family:Arial, Helvetica, sans-serif;color:#f97316;font-size:13px;font-weight:bold;letter-spacing:1px;text-transform:uppercase;\">New Portfolio Message</span>" +
            "</td></tr>" +

            // Heading
            "<tr><td style=\"padding:12px 32px 0 32px;\">" +
            "<h1 style=\"margin:0;font-family:Arial, Helvetica, sans-serif;color:#ffffff;font-size:22px;font-weight:bold;\">" + safeName + " sent you a message</h1>" +
            "</td></tr>" +

            // Divider
            "<tr><td style=\"padding:16px 32px 0 32px;\">" +
            "<div style=\"height:2px;width:56px;background-color:#c2560f;border-radius:2px;\"></div>" +
            "</td></tr>" +

            // Sender details
            "<tr><td style=\"padding:20px 32px 0 32px;\">" +
            "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\">" +
            "<tr>" +
            "<td style=\"padding:0 0 6px 0;font-family:Arial, Helvetica, sans-serif;color:#6b7280;font-size:12px;text-transform:uppercase;letter-spacing:0.5px;\">From</td>" +
            "</tr>" +
            "<tr>" +
            "<td style=\"padding:0 0 16px 0;font-family:Arial, Helvetica, sans-serif;color:#ffffff;font-size:15px;\">" + safeName + " &lt;<a href=\"mailto:" + safeEmail + "\" style=\"color:#f97316;text-decoration:none;\">" + safeEmail + "</a>&gt;</td>" +
            "</tr>" +
            "</table>" +
            "</td></tr>" +

            // Quoted message box
            "<tr><td style=\"padding:0 32px 0 32px;\">" +
            "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" bgcolor=\"#0e1b26\" " +
            "style=\"background-color:#0e1b26;border:1px solid rgba(255,255,255,0.08);border-radius:8px;\">" +
            "<tr><td style=\"padding:16px 20px;\">" +
            "<p style=\"margin:0;font-family:Arial, Helvetica, sans-serif;color:#d1d5db;font-size:14px;line-height:1.6;\">" + safeMessage + "</p>" +
            "</td></tr>" +
            "</table>" +
            "</td></tr>" +

            // Reply button
            "<tr><td style=\"padding:24px 32px 32px 32px;\">" +
            "<a href=\"" + mailtoLink + "\" " +
            "style=\"display:inline-block;font-family:Arial, Helvetica, sans-serif;background-color:#f97316;color:#000000;font-size:14px;font-weight:bold;text-decoration:none;padding:12px 24px;border-radius:6px;\">" +
            "Reply to " + safeName +
            "</a>" +
            "</td></tr>" +

            "</table>" +
            "</td></tr>" +
            "</table>" +
            "</body></html>";
    }

    private String buildAutoReplyHtml(ContactRequest request) {
        // Inline styles throughout — most email clients strip <style> blocks
        // in <head>, so every rule has to live on the element itself.
        String safeName = escapeHtml(request.getName());
        String safeMessage = escapeHtml(request.getMessage());

        return "<!DOCTYPE html>" +
            "<html><head>" +
            "<meta charset=\"UTF-8\">" +
            "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
            "<meta name=\"format-detection\" content=\"telephone=no\">" +
            "</head>" +
            "<body style=\"margin:0;padding:0;background-color:#0e1b26;font-family:Arial, Helvetica, sans-serif;\">" +
            "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" bgcolor=\"#0e1b26\" style=\"background-color:#0e1b26;padding:40px 0;\">" +
            "<tr><td align=\"center\">" +

            "<table role=\"presentation\" width=\"480\" cellpadding=\"0\" cellspacing=\"0\" bgcolor=\"#182329\" " +
            "style=\"background-color:#182329;border-radius:12px;border:1px solid rgba(255,255,255,0.08);overflow:hidden;\">" +

            // Header bar
            "<tr><td style=\"padding:28px 32px 0 32px;\">" +
            "<span style=\"font-family:Arial, Helvetica, sans-serif;color:#f97316;font-size:13px;font-weight:bold;letter-spacing:1px;text-transform:uppercase;\">Santhoshkumar Raman</span>" +
            "</td></tr>" +

            // Heading
            "<tr><td style=\"padding:12px 32px 0 32px;\">" +
            "<h1 style=\"margin:0;font-family:Arial, Helvetica, sans-serif;color:#ffffff;font-size:22px;font-weight:bold;\">Thanks for reaching out, " + safeName + "</h1>" +
            "</td></tr>" +

            // Divider
            "<tr><td style=\"padding:16px 32px 0 32px;\">" +
            "<div style=\"height:2px;width:56px;background-color:#c2560f;border-radius:2px;\"></div>" +
            "</td></tr>" +

            // Body copy
            "<tr><td style=\"padding:20px 32px 0 32px;\">" +
            "<p style=\"margin:0;font-family:Arial, Helvetica, sans-serif;color:#9ca3af;font-size:14px;line-height:1.6;\">" +
            "I've received your message and will get back to you soon. Here's a copy of what you sent, for your records:" +
            "</p>" +
            "</td></tr>" +

            // Quoted message box
            "<tr><td style=\"padding:16px 32px 0 32px;\">" +
            "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" bgcolor=\"#0e1b26\" " +
            "style=\"background-color:#0e1b26;border:1px solid rgba(255,255,255,0.08);border-radius:8px;\">" +
            "<tr><td style=\"padding:16px 20px;\">" +
            "<p style=\"margin:0;font-family:Arial, Helvetica, sans-serif;color:#d1d5db;font-size:14px;line-height:1.6;font-style:italic;\">\u201C" + safeMessage + "\u201D</p>" +
            "</td></tr>" +
            "</table>" +
            "</td></tr>" +

            // Signature
            "<tr><td style=\"padding:24px 32px 0 32px;\">" +
            "<p style=\"margin:0;font-family:Arial, Helvetica, sans-serif;color:#9ca3af;font-size:14px;line-height:1.6;\">Best,<br/>" +
            "<span style=\"color:#ffffff;font-weight:bold;\">Santhoshkumar Raman</span></p>" +
            "</td></tr>" +

            // Social links footer
            "<tr><td style=\"padding:20px 32px 0 32px;\">" +
            "<a href=\"https://github.com/SanthoshKumar2412\" style=\"font-family:Arial, Helvetica, sans-serif;color:#f97316;font-size:13px;text-decoration:none;font-weight:bold;\">GitHub</a>" +
            "<span style=\"color:#4b5563;font-size:13px;\">&nbsp;&nbsp;•&nbsp;&nbsp;</span>" +
            "<a href=\"https://www.linkedin.com/in/santhoshkumar-r-b06030252\" style=\"font-family:Arial, Helvetica, sans-serif;color:#f97316;font-size:13px;text-decoration:none;font-weight:bold;\">LinkedIn</a>" +
            "<span style=\"color:#4b5563;font-size:13px;\">&nbsp;&nbsp;•&nbsp;&nbsp;</span>" +
            "<a href=\"https://santhoshkumar-dev-portfolio.netlify.app\" style=\"font-family:Arial, Helvetica, sans-serif;color:#f97316;font-size:13px;text-decoration:none;font-weight:bold;\">Portfolio</a>" +
            "</td></tr>" +

            // Automated-message note
            "<tr><td style=\"padding:20px 32px 32px 32px;\">" +
            "<p style=\"margin:0;font-family:Arial, Helvetica, sans-serif;color:#4b5563;font-size:12px;line-height:1.5;\">" +
            "This is an automated confirmation — a personal reply will follow separately." +
            "</p>" +
            "</td></tr>" +

            "</table>" +
            "</td></tr>" +
            "</table>" +
            "</body></html>";
    }

    /**
     * Minimal HTML-escaping so form input can never break out of the markup
     * or inject scripts/tags into the email body.
     */
    private String escapeHtml(String input) {
        if (input == null) return "";
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}