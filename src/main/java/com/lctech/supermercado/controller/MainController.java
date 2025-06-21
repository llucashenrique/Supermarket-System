package com.lctech.supermercado.controller;

import com.lctech.supermercado.gui.SalesController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import com.lctech.supermercado.config.SpringFXMLLoader;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@Controller
public class MainController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @Autowired
    private SpringFXMLLoader springFXMLLoader;

    @FXML
    public void initialize() {
        // Configura o evento Enter para o campo de senha
        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                handleLogin(new ActionEvent());
            }
        });
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Erro", "Por favor, preencha todos os campos.", Alert.AlertType.ERROR);
            return;
        }

        if (authenticate(username, password)) {
            System.out.println("Login bem-sucedido! Redirecionando para o Dashboard...");
            switchToDashboard();
        } else {
            showAlert("Login Falhou", "Usuário ou senha inválidos.", Alert.AlertType.ERROR);
        }
    }

    private void switchToDashboard() {
        try {
            FXMLLoader loader = springFXMLLoader.load("/views/SalesView.fxml");
            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle("Registrar Venda - Sistema de Supermercado");

            SalesController salesController = loader.getController();
            salesController.initializeData("Usuário autenticado: " + usernameField.getText());

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erro", "Não foi possível carregar a tela de Registrar Venda.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        showAlert("Registrar", "Funcionalidade de registro ainda não implementada.", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleExit(ActionEvent event) {
        System.exit(0);
    }

    private boolean authenticate(String username, String password) {
        try {
            URL url = new URL("http://localhost:8080/login");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; utf-8");
            connection.setDoOutput(true);

            String jsonInput = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInput.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            return responseCode == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
