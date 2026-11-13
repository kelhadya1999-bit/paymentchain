package com.paymentchain.controller;

import com.paymentchain.service.JwtService;
import com.paymentchain.service.KeycloakRestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.*;

@RestController
@RequestMapping("/auth")
public class IndexController {

    private static final Logger logger = LoggerFactory.getLogger(IndexController.class);

    @Autowired
    private KeycloakRestService restService;

    @Autowired
    private JwtService jwtService;

    public record LoginRequest(String username, String password) {}
    public record TokenRequest(String refreshToken) {}

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            return json(restService.login(request.username(), request.password()));
        } catch (HttpStatusCodeException e) {
            return keycloakError(e);
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody TokenRequest request) {
        try {
            return json(restService.refresh(request.refreshToken()));
        } catch (HttpStatusCodeException e) {
            return keycloakError(e);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody TokenRequest request) {
        try {
            restService.logout(request.refreshToken());
            return ResponseEntity.noContent().build();
        } catch (HttpStatusCodeException e) {
            return keycloakError(e);
        }
    }

    // El token llega como "Bearer xxxxx"
    @GetMapping("/check")
    public ResponseEntity<?> check(@RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            return json(restService.checkValidity(authorization));
        } catch (HttpStatusCodeException e) {
            return keycloakError(e);
        }
    }

    @GetMapping("/roles")
    public ResponseEntity<?> getRoles(@RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            return ResponseEntity.ok(restService.getRoles(authorization));
        } catch (HttpStatusCodeException e) {
            return keycloakError(e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Validar la firma del token en local, con la clave pública del realm
    @GetMapping("/validate")
    public ResponseEntity<?> validate(@RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        boolean valid = jwtService.isValid(authorization.substring(7));
        return valid ? ResponseEntity.ok().build()
                : ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    private ResponseEntity<String> json(String body) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(body);
    }

    private ResponseEntity<String> keycloakError(HttpStatusCodeException e) {
        logger.warn("Keycloak respondió {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
        return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
    }
}