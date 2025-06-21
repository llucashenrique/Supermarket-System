package com.lctech.supermercado.config;

import javafx.fxml.FXMLLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;


@Component
public class SpringFXMLLoader {

    @Autowired
    private ApplicationContext context;

    public FXMLLoader load(String fxmlPath) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        loader.setControllerFactory(context::getBean); // Configura o Spring como fábrica de controladores
        return loader;
    }
}

