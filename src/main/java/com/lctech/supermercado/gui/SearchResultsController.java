package com.lctech.supermercado.gui;

import com.fazecast.jSerialComm.SerialPort;
import com.lctech.supermercado.controller.ProductsControllerFX;
import com.lctech.supermercado.controller.QuantityInputController;
import com.lctech.supermercado.device.BalancaController;
import com.lctech.supermercado.model.Product;
import com.lctech.supermercado.repository.OrderRepository;
import com.lctech.supermercado.repository.ProductsRepository;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Controller
public class SearchResultsController {

    private final ApplicationContext applicationContext;
    private final ProductsRepository productsRepository;
    private final OrderRepository ordersRepository;
    private Product selectedProduct;

    @FXML
    private TextField searchField;

    @FXML
    private ListView<String> resultsList;

    private double selectedQuantity = 0;

    public double getSelectedQuantity() {
        return selectedQuantity;
    }

    private SerialPort serialPort;

    private SalesController salesController;

    private final BalancaController balancaController = BalancaController.getInstancia(); // ✅ CERTO



    public void setSalesController(SalesController salesController) {
        this.salesController = salesController;
    }

    private Stage dialogStage;

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }


    public SearchResultsController(ApplicationContext applicationContext,
                                   ProductsRepository productsRepository,
                                   OrderRepository ordersRepository) {
        this.applicationContext = applicationContext;
        this.productsRepository = productsRepository;
        this.ordersRepository = ordersRepository;
    }

    @FXML
    public void initialize() {
        balancaController.resetarLeitura(); // ativa o listener e zera a variável


        // Atualiza os resultados dinamicamente enquanto o usuário digita
        searchField.textProperty().addListener((observable, oldValue, newValue) -> searchProducts(newValue));

        // Permite navegar pela lista com as setas e manter o foco no campo de texto
        searchField.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case DOWN -> {
                    if (!resultsList.getItems().isEmpty()) {
                        resultsList.requestFocus();
                        if (resultsList.getSelectionModel().getSelectedIndex() == -1) {
                            resultsList.getSelectionModel().select(0); // Seleciona o primeiro item
                        } else {
                            resultsList.getSelectionModel().selectNext(); // Move para o próximo item
                        }
                    }
                }
                case UP -> {
                    if (!resultsList.getItems().isEmpty()) {
                        resultsList.requestFocus();
                        resultsList.getSelectionModel().selectPrevious(); // Move para o item anterior
                    } else {
                        searchField.requestFocus();
                    }
                }
                case F11 -> openProductDetails(); // Adiciona funcionalidade para F11
                default -> {
                    if (event.getCode() != KeyCode.F11) {
                        searchField.requestFocus();
                    }
                }
            }
        });

        // Configura ações na lista
        resultsList.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ENTER -> handleSelectItem(); // Seleciona o item ao pressionar Enter
                case DOWN -> {
                    if (resultsList.getSelectionModel().getSelectedIndex() < resultsList.getItems().size() - 1) {
                        resultsList.getSelectionModel().selectNext(); // Move para o próximo item
                    }
                }
                case UP -> {
                    if (resultsList.getSelectionModel().getSelectedIndex() > 0) {
                        resultsList.getSelectionModel().selectPrevious(); // Move para o item anterior
                    } else {
                        searchField.requestFocus();
                    }
                }
                case F11 -> openProductDetails(); // Abre os detalhes do produto ao pressionar F11
                default -> {
                }
            }
        });

        // Clique duplo para selecionar um item
        resultsList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                handleSelectItem();
            }
        });

        // Permite fechar a janela com ESC
        searchField.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.ESCAPE) {
                        searchField.getScene().getWindow().hide();
                    }
                });
            }
        });
    }


    @FXML
    private void handleProductSearch() {
        try {
            // Configura o FXMLLoader para carregar o arquivo FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/SearchResultsView.fxml"));

            // Configura o Spring para gerenciar o controlador
            loader.setControllerFactory(applicationContext::getBean);

            // Carrega a interface do SearchResultsView
            Parent root = loader.load();

            // Obtém o controlador do SearchResultsView
            SearchResultsController controller = loader.getController();
            controller.initializeSearch(productsRepository.findAll()); // Passa a lista de produtos para o controlador

            // Configura e exibe a nova janela
            Stage stage = new Stage();
            stage.setTitle("Resultados da Pesquisa");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL); // Modal bloqueia a interação com outras janelas
            stage.showAndWait();

            // Recupera o produto selecionado no SearchResultsController (se houver)
            Product selectedProduct = controller.getSelectedProduct();
            if (selectedProduct != null) {
                // Processa o produto selecionado (exemplo: exibir detalhes ou salvar em outro campo)
                this.selectedProduct = selectedProduct;

                // Exemplo de processamento: Exibe um alerta com o nome do produto
                showAlert("Produto Selecionado", "Produto: " + selectedProduct.getName(), Alert.AlertType.INFORMATION);
            }
        } catch (IOException e) {
            // Mostra uma mensagem de erro em caso de falha no carregamento do FXML
            showAlert("Erro", "Erro ao carregar a tela de resultados de pesquisa: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }


    public void initializeSearch(List<Product> products) {
        updateResults(products);
    }

    private void updateResults(List<Product> products) {
        resultsList.getItems().clear();
        if (products != null && !products.isEmpty()) {
            products.forEach(product -> resultsList.getItems().add(
                    String.format("%s (ID: %d) - R$ %.2f", product.getName(), product.getId(), product.getPrice())
            ));
        }
    }

    public void searchProducts(String query) {
        if (query.isEmpty()) {
            resultsList.getItems().clear();
            return;
        }

        try {
            List<Product> products;

            if (query.matches("\\d+")) {
                products = productsRepository.findById(Long.parseLong(query))
                        .map(Collections::singletonList)
                        .orElse(Collections.emptyList());
            } else {
                products = productsRepository.findByNameContainingIgnoreCase(query);
            }

            updateResults(products);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSelectItem() {
        String selectedItem = resultsList.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            try {
                String idPart = selectedItem.split(" \\(ID: ")[1].split("\\)")[0];
                Long productId = Long.parseLong(idPart);

                Product product = productsRepository.findById(productId).orElse(null);

                if (product != null) {
                    if ("Kg".equals(product.getTipo())) {
                        BalancaController.getInstancia().resetarLeitura();

                        double peso = 0;
                        long inicio = System.currentTimeMillis();
                        while ((System.currentTimeMillis() - inicio) < 2000) {
                            peso = BalancaController.getInstancia().obterPesoLido();
                            if (peso > 0) break;
                            Thread.sleep(100);
                        }

                        if (peso <= 0) {
                            showAlert("Erro", "Produto não detectado na balança ou a balança não conseguiu ler o peso.", Alert.AlertType.ERROR);
                            return;
                        }

                        selectedQuantity = peso;
                        salesController.addCartItem(product, selectedQuantity);
                        dialogStage.close();

                    } else {
                        requestQuantityIfNeeded(product);
                    }

                    if (selectedQuantity > 0) {
                        this.selectedProduct = product;
                        if (dialogStage != null) {
                            dialogStage.close();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void requestQuantityIfNeeded(Product product) {
        // Verifica se o produto é do tipo "Unidade"
        if (!"Kg".equals(product.getTipo())) {
            try {
                // Exibe a tela para inserir a quantidade do produto
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/QuantityInputView.fxml"));
                loader.setControllerFactory(applicationContext::getBean);

                Parent root = loader.load();

                QuantityInputController controller = loader.getController();
                controller.setStock(product.getStock());  // Passa o estoque atual para o controlador

                Stage stage = new Stage();
                stage.setTitle("Quantidade de Itens");
                stage.setScene(new Scene(root));
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.showAndWait();

                double quantity = controller.getQuantity(); // Obtém a quantidade inserida

                // Verifica se a quantidade é válida
                if (quantity > 0) {
                    selectedQuantity = quantity;
                    // Adiciona o item ao carrinho
                    if (salesController != null) {
                        salesController.addCartItem(product, selectedQuantity); // Adiciona o produto com a quantidade ao carrinho
                    }
                } else {
                    showAlert("Erro", "Quantidade inválida para produto por unidade.", Alert.AlertType.ERROR);
                }
            } catch (IOException e) {
                showAlert("Erro", "Erro ao obter a quantidade de unidades: " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }




    private void requestQuantityForUnit(Product product) {
        try {
            // Pede a quantidade de unidades ao usuário
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/QuantityInputView.fxml"));
            loader.setControllerFactory(applicationContext::getBean);

            Parent root = loader.load();

            QuantityInputController controller = loader.getController();
            controller.setStock(product.getStock());  // Passa o estoque atual para o controlador

            Stage stage = new Stage();
            stage.setTitle("Quantidade de Itens");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            double quantity = controller.getQuantity(); // Obtém a quantidade inserida

            // Verifica se a quantidade é válida
            if (quantity > 0) {
                selectedQuantity = quantity;
                // Adiciona o item ao carrinho
                if (salesController != null) {
                    salesController.addCartItem(product, selectedQuantity); // Adiciona o produto com a quantidade ao carrinho
                }
            } else {
                showAlert("Erro", "Quantidade inválida para produto por unidade.", Alert.AlertType.ERROR);
            }
        } catch (IOException e) {
            showAlert("Erro", "Erro ao obter a quantidade de unidades: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }


    private void openProductDetails() {
        String selectedItem = resultsList.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showAlert("Erro", "Nenhum produto selecionado para visualização.", Alert.AlertType.ERROR);
            return;
        }

        try {
            String idPart = selectedItem.split(" \\(ID: ")[1].split("\\)")[0];
            Long productId = Long.parseLong(idPart);

            Product product = productsRepository.findById(productId).orElse(null);
            if (product == null) {
                showAlert("Erro", "Produto não encontrado.", Alert.AlertType.ERROR);
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/product-view.fxml"));

            // Configura o Spring para injetar o controlador
            loader.setControllerFactory(applicationContext::getBean);

            Parent root = loader.load();

            // Obtém o controlador do product-view e configura o produto
            ProductsControllerFX controller = loader.getController();
            controller.setSelectedProduct(product);

            // Abre a nova janela
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Detalhes do Produto");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (IOException e) {
            showAlert("Erro", "Erro ao abrir os detalhes do produto: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }


    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/DashBoardView.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

        } catch (IOException e) {
            showAlert("Erro", "Não foi possível carregar a tela do Dashboard.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClose() {
        searchField.getScene().getWindow().hide();
    }


    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public Product getSelectedProduct() {
        return selectedProduct;
    }
}

