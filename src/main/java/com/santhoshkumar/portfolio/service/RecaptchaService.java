package com.santhoshkumar.portfolio.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class RecaptchaService {

    @Value("${google.recaptcha.secret}")
    private String recaptchaSecret;

    private static final String VERIFY_URL =
        "https://www.google.com/recaptcha/api/siteverify";

    public boolean verify(String token) {

        RestTemplate restTemplate = new RestTemplate();

        Map response = restTemplate.postForObject(
            VERIFY_URL + "?secret=" + recaptchaSecret + "&response=" + token,
            null,
            Map.class
        );

        return response != null && Boolean.TRUE.equals(response.get("success"));
    }
}
