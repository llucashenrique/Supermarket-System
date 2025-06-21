package com.lctech.supermercado;

import com.lctech.supermercado.gui.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class SpringJavaFXApplication extends Application {

    private ConfigurableApplicationContext context;
    private SceneManager sceneManager;


    @Override
    public void init() {
        context = new SpringApplicationBuilder(SupermercadoApplication.class).run();
    }

    @Override
    public void start(Stage primaryStage) {
        // Inicializa o SceneManager e define a tela principal
        sceneManager = new SceneManager(context);
        sceneManager.setPrimaryStage(primaryStage);

        // Troca para a tela inicial
        sceneManager.switchToMainView();
    }

    @Override
    public void stop() {
        // Encerra o contexto do Spring ao fechar o aplicativo
        context.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}


