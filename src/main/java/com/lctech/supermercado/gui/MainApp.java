package com.lctech.supermercado.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainApp extends Application {

    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        showLoginView();
    }

    private void showLoginView() {
        try {
            System.out.println("Carregando FXML: /views/mainView.fxml");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/mainView.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());

            primaryStage.setTitle("Tela de Login - Supermercado");
            primaryStage.setScene(scene);
            primaryStage.show();

            System.out.println("Tela de login carregada com sucesso.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erro", "Não foi possível carregar a tela de login.", Alert.AlertType.ERROR);
        }
    }

    public void showDashboardView() {
        try {
            System.out.println("Carregando FXML: /views/dashboardView.fxml");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/dashboardView.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());

            primaryStage.setTitle("Dashboard - Supermercado");
            primaryStage.setScene(scene);

            System.out.println("Dashboard carregado com sucesso.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erro", "Não foi possível carregar o Dashboard.", Alert.AlertType.ERROR);
        }
    }

    public boolean authenticate(String username, String password) {
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
            return responseCode == 200; // Retorna true se o código de resposta for 200 (OK)

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erro", "Não foi possível conectar ao servidor.", Alert.AlertType.ERROR);
            return false; // Retorna false em caso de erro
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

