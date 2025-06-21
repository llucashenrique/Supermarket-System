package com.lctech.supermercado;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.lctech.supermercado.repository")
public class SupermercadoApplication extends Application {

	private ConfigurableApplicationContext springContext;

	@Override
	public void init() {
		springContext = new SpringApplicationBuilder(SupermercadoApplication.class).run();
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/SalesView.fxml"));
		loader.setControllerFactory(springContext::getBean); // injeta controladores com Spring
		Parent root = loader.load();

		Scene scene = new Scene(root);
		scene.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm()); // ✅ CSS

		primaryStage.setTitle("Sistema de Vendas - Supermercado");
		primaryStage.setScene(scene);
		primaryStage.setResizable(false);
		primaryStage.show();
	}

	@Override
	public void stop() {
		springContext.close(); // encerra contexto Spring ao fechar app
	}

	public static void main(String[] args) {
		launch(args); // inicia JavaFX
	}
}
