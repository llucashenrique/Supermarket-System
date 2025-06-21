package com.lctech.supermercado.controller;

import com.lctech.supermercado.dto.LoginRequest;
import com.lctech.supermercado.model.User;
import com.lctech.supermercado.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/login")
public class LoginController {

    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    public LoginController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {
        logger.info("Attempting login for user: {}", loginRequest.getUsername());

        Optional<User> optionalUser = userRepository.findByUsername(loginRequest.getUsername());

        if (optionalUser.isEmpty()) {
            logger.warn("Login failed: User {} not found", loginRequest.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciais inválidas: Usuário não encontrado.");
        }

        User user = optionalUser.get();

        if (!user.getPassword().equals(loginRequest.getPassword())) {
            logger.warn("Login failed: Incorrect password for user {}", loginRequest.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciais inválidas: Senha incorreta.");
        }

        logger.info("Login successful for user: {}", loginRequest.getUsername());
        return ResponseEntity.ok("Login bem-sucedido! Bem-vindo, " + user.getUsername());
    }
}
