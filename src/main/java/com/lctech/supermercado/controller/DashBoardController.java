package com.lctech.supermercado.controller;

import com.lctech.supermercado.config.ApplicationContextProvider;
import com.lctech.supermercado.config.SpringFXMLLoader;
import com.lctech.supermercado.repository.CompanyConfigRepository;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Controller;

import java.io.IOException;

@Controller
@RequiredArgsConstructor
public class DashBoardController implements ApplicationContextAware {

    private final SpringFXMLLoader springFXMLLoader;
    private ApplicationContext applicationContext;

    @FXML
    private BorderPane rootPane;

    public void setApplicationContext(ApplicationContext context) {
        this.applicationContext = context;
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        switchToScene("/views/MainView.fxml", event, "Login");
    }

    @FXML
    private void handleRegisterSale(ActionEvent event) {
        switchToScene("/views/SalesView.fxml", event, "Registrar Venda");
    }

    @FXML
    private void handleManageProducts(ActionEvent event) {
        switchToScene("/views/Product-view.fxml", event, "Gerenciar Produtos");
    }

    @FXML
    private void handleExit(ActionEvent event) {
        System.exit(0);
    }


    @FXML
    private void handleRegisterCustomer(ActionEvent event) {
        switchToScene("/views/CustomerView.fxml", event, "Cadastro de Clientes");
    }

    @FXML
    private void handleClienteConta() {
        openNewWindow("/views/ClienteContaView.fxml", "Conta do Cliente");
    }

    @FXML
    private void handleOpenAnalytics() {
        openNewWindow("/views/SalesAnalyticsView.fxml", "Analytics de Vendas");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void switchToScene(String fxmlPath, ActionEvent event, String title) {
        try {
            Parent root = springFXMLLoader.load(fxmlPath).load();
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            showAlert("Erro", "Não foi possível carregar a tela: " + fxmlPath, Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void openNewWindow(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(applicationContext::getBean); // para funcionar com Spring
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Erro", "Não foi possível abrir a tela: " + title, Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void abrirConfiguracoesEmpresa() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/EmpresaConfigView.fxml"));
            Parent root = loader.load();

            EmpresaConfigController controller = loader.getController();
            controller.setCompanyConfigRepository(ApplicationContextProvider.getContext().getBean(CompanyConfigRepository.class));

            Stage stage = new Stage();
            stage.setTitle("Configurações da Empresa");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
