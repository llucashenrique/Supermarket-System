package com.lctech.supermercado.controller;

import com.lctech.supermercado.model.User;
import com.lctech.supermercado.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody User user) {
        logger.info("Attempting to register user: {}", user.getUsername());

        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            logger.warn("User already exists: {}", user.getUsername());
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Usuário já existe");
        }

        userRepository.save(user);
        logger.info("User registered successfully: {}", user.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body("Usuário registrado com sucesso");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User user) {
        logger.info("Login attempt for user: {}", user.getUsername());

        Optional<User> optionalUser = userRepository.findByUsername(user.getUsername());

        if (optionalUser.isPresent() && optionalUser.get().getPassword().equals(user.getPassword())) {
            logger.info("Login successful for user: {}", user.getUsername());
            return ResponseEntity.ok("Login bem-sucedido! Bem-vindo, " + user.getUsername());
        }

        logger.warn("Invalid credentials for user: {}", user.getUsername());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciais inválidas");
    }
}
