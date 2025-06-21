package com.lctech.supermercado.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.stereotype.Controller;

@Controller
public class QuantityInputController {

    @FXML
    private Label stockLabel;

    @FXML
    private TextField quantityField;

    private int quantity = 1; // Valor padrão
    private Double stock; // Estoque disponível

    public void setStock(Double stock) {
        this.stock = stock;
        if (stockLabel != null) {
            stockLabel.setText("Estoque disponível: " + stock);
        }
    }

    @FXML
    public void initialize() {
        // Validações de entrada
        quantityField.textProperty().addListener((observable, oldValue, newValue) -> {
            // Permite apenas números positivos
            if (!newValue.matches("\\d*")) {
                quantityField.setText(oldValue);
            } else if (!newValue.isEmpty() && Integer.parseInt(newValue) > stock) {
                // Impede que a quantidade ultrapasse o estoque disponível
                quantityField.setText(oldValue);
            }
        });

        // Atalhos para Enter e ESC
        quantityField.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.setOnKeyPressed(event -> {
                    switch (event.getCode()) {
                        case ENTER -> handleConfirm();
                        case ESCAPE -> handleCancel();
                    }
                });
            }
        });
    }

    @FXML
    private void handleConfirm() {
        if (!quantityField.getText().isEmpty()) {
            int enteredQuantity = Integer.parseInt(quantityField.getText());

            if (enteredQuantity <= 0) {
                showAlert("Erro", "A quantidade deve ser maior que 0.");
                return; // Não fecha a janela
            }

            quantity = enteredQuantity; // Define a quantidade escolhida
            closeWindow();
        } else {
            showAlert("Erro", "Por favor, insira uma quantidade válida.");
        }
    }


    @FXML
    private void handleCancel() {
        quantity = 0; // Define 0 se o usuário cancelar
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) quantityField.getScene().getWindow();
        stage.close();
    }

    public int getQuantity() {
        return quantity;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

