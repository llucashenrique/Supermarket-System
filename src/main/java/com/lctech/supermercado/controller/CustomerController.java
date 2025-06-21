package com.lctech.supermercado.controller;

import com.lctech.supermercado.service.CnpjService;
import com.lctech.supermercado.service.CustomerService;
import com.lctech.supermercado.config.SpringFXMLLoader;
import com.lctech.supermercado.model.Customers;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CnpjService cnpjService;

    @FXML
    private TextField nameField;

    @FXML
    private TextField addressField;

    @FXML
    private TextField phoneNumberField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField cpfField;

    @FXML
    private TextField cnpjField;

    @Autowired
    private SpringFXMLLoader springFXMLLoader;

    // Método para cadastrar cliente
    @FXML
    private void handleAddCustomer(ActionEvent event) {
        try {
            String name = nameField.getText().trim();
            String address = addressField.getText().trim();
            String phone = phoneNumberField.getText().trim();
            String email = emailField.getText().trim();
            String cpf = cpfField.getText().trim();
            String cnpj = cnpjField.getText().trim();

            if (name.isEmpty() || address.isEmpty() || phone.isEmpty() || email.isEmpty() || (cpf.isEmpty() && cnpj.isEmpty())) {
                showAlert("Erro", "Todos os campos devem ser preenchidos. CPF ou CNPJ é obrigatório.", Alert.AlertType.ERROR);
                return;
            }

            if (!cpf.isEmpty() && !cpf.matches("\\d{11}")) {
                showAlert("Erro", "CPF deve conter 11 dígitos.", Alert.AlertType.ERROR);
                return;
            }

            if (!cnpj.isEmpty() && !cnpj.matches("\\d{14}")) {
                showAlert("Erro", "CNPJ deve conter 14 dígitos.", Alert.AlertType.ERROR);
                return;
            }

            Customers customer = Customers.builder()
                    .name(name)
                    .address(address)
                    .phone(phone)
                    .email(email)
                    .cpf(cpf.isEmpty() ? null : cpf)
                    .cnpj(cnpj.isEmpty() ? null : cnpj)
                    .build();

            customerService.createCustomer(customer);
            showAlert("Sucesso", "Cliente cadastrado com sucesso!", Alert.AlertType.INFORMATION);
            clearFields();
        } catch (Exception e) {
            showAlert("Erro", "Erro ao cadastrar cliente: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSearchByCnpj(ActionEvent event) {
        try {
            String cnpj = cnpjField.getText().trim();

            if (cnpj.isEmpty() || !cnpj.matches("\\d{14}")) {
                showAlert("Erro", "CNPJ inválido. Insira um CNPJ com 14 dígitos.", Alert.AlertType.ERROR);
                return;
            }

            Optional<Customers> optionalCustomer = cnpjService.fetchCustomerByCnpj(cnpj);

            if (optionalCustomer.isPresent()) {
                Customers customer = optionalCustomer.get();
                nameField.setText(customer.getName());
                addressField.setText(customer.getAddress());
                phoneNumberField.setText(customer.getPhone());
                emailField.setText(customer.getEmail());
                showAlert("Sucesso", "Dados encontrados com sucesso!", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Erro", "Nenhum cliente encontrado com esse CNPJ.", Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            showAlert("Erro", "Erro ao buscar dados: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }


    // Método para voltar ao Dashboard
    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = springFXMLLoader.load("/views/DashBoardView.fxml");
            Parent root = loader.load();

            Stage stage = (Stage) nameField.getScene().getWindow(); // Obtém o Stage atual.
            stage.setScene(new Scene(root));
            stage.setTitle("Dashboard");
            stage.show();
        } catch (IOException e) {
            showAlert("Erro", "Não foi possível carregar a tela do Dashboard.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    // Limpa os campos
    private void clearFields() {
        nameField.clear();
        addressField.clear();
        phoneNumberField.clear();
        emailField.clear();
        cpfField.clear();
        cnpjField.clear();
    }

    // Exibe um alerta
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
