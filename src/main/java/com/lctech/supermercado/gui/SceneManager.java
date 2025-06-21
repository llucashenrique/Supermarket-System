package com.lctech.supermercado.gui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.function.Consumer;

public class SceneManager {

    private final ConfigurableApplicationContext context;
    private Stage primaryStage;

    public SceneManager(ConfigurableApplicationContext context) {
        this.context = context;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void switchToMainView() {
        switchScene("/views/MainView.fxml", "Supermercado - Principal", null);
    }

    public void switchToProductsView() {
        switchScene("/views/Product-view.fxml", "Supermercado - Produtos", null);
    }

    public void switchToSalesView(Consumer<SalesController> initializer) {
        switchScene("/views/SalesView.fxml", "Registrar Venda - Sistema de Supermercado", controller -> {
            if (controller instanceof SalesController salesController) {
                initializer.accept(salesController);
            }
        });
    }

    private void switchScene(String fxmlPath, String title, Consumer<Object> controllerInitializer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(context::getBean);
            Parent root = loader.load();

            // Pega o controller e aplica custom init se fornecido
            if (controllerInitializer != null) {
                Object controller = loader.getController();
                controllerInitializer.accept(controller);
            }

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());

            primaryStage.setScene(scene);
            primaryStage.setTitle(title);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erro ao carregar a cena: " + fxmlPath);
        }
    }
}
