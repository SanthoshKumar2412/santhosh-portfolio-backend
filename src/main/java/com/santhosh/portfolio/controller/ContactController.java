package com.santhosh.portfolio.controller;

import com.santhosh.portfolio.dto.ApiResponse;
import com.santhosh.portfolio.dto.ContactRequest;
import com.santhosh.portfolio.service.EmailService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contact")
public class ContactController {

    private static final Logger log = LoggerFactory.getLogger(ContactController.class);

    private final EmailService emailService;

    public ContactController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse> submitContactForm(@Valid @RequestBody ContactRequest request) {

        // Honeypot check: if this hidden field has a value, it was filled in by a bot.
        // Return a fake "success" so bots don't learn to look elsewhere, but skip sending any email.
        if (request.getWebsite() != null && !request.getWebsite().isBlank()) {
            log.warn("Honeypot triggered for submission from {}", request.getEmail());
            return ResponseEntity.ok(new ApiResponse(true, "Message sent successfully!"));
        }

        // Fires in the background (see @Async on EmailService) — we don't
        // block the HTTP response waiting for Gmail's SMTP handshake.
        // Any send failure is logged server-side rather than surfaced here.
        emailService.sendContactEmails(request);

        return ResponseEntity.ok(new ApiResponse(true, "Message sent successfully!"));
    }
}
