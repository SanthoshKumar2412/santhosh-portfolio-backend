package com.santhoshkumar.portfolio.controller;

import com.santhoshkumar.portfolio.dto.ContactRequest;
import com.santhoshkumar.portfolio.service.EmailService;
import com.santhoshkumar.portfolio.service.RateLimitService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:4200","https://santhoshkumar-dev-portfolio.netlify.app"})
public class ContactController {

    private final EmailService emailService;
    private final RateLimitService rateLimitService;

    public ContactController(
            EmailService emailService,
            RateLimitService rateLimitService
    ) {
        this.emailService = emailService;
        this.rateLimitService = rateLimitService;
    }

    @PostMapping("/contact")
    public ResponseEntity<String> sendContact(
            @Valid @RequestBody ContactRequest request,
            HttpServletRequest httpRequest
    ) {

        /* 1️⃣ Rate-limit (ANTI-SPAM) */
        String clientIp = httpRequest.getRemoteAddr();
        var bucket = rateLimitService.resolveBucket(clientIp);

        if (!bucket.tryConsume(1)) {
            return ResponseEntity
                    .status(429)
                    .body("Too many messages. Please wait 1 minute.");
        }

        /* 3️⃣ Send email */
        String subject = "New Contact Message from Portfolio";
        String body =
                "Name: " + request.getName() + "\n" +
                "Email: " + request.getEmail() + "\n\n" +
                "Message:\n" + request.getMessage();

        emailService.sendAdminMail(
        	    "santhoshkumar2002.raman@gmail.com",
        	    subject,
        	    request.getName(),
        	    request.getEmail(),
        	    request.getMessage()
        	);
        emailService.sendAutoReply(
                request.getEmail(),
                request.getName()
            );


        return ResponseEntity.ok("Message sent successfully");
    }
}
