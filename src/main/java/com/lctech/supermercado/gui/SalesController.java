package com.lctech.supermercado.gui;

import com.fazecast.jSerialComm.SerialPort;
import com.lctech.supermercado.device.BalancaController;
import com.lctech.supermercado.model.CartItem;
import com.lctech.supermercado.model.Customers;
import com.lctech.supermercado.model.Product;
import com.lctech.supermercado.repository.CompanyConfigRepository;
import com.lctech.supermercado.repository.CustomerRepository;
import com.lctech.supermercado.repository.OrderRepository;
import com.lctech.supermercado.repository.ProductsRepository;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import com.fazecast.jSerialComm.SerialPort;


import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@Controller
public class SalesController {

    private final ApplicationContext applicationContext;
    private final ProductsRepository productsRepository;
    private final ObservableList<CartItem> cart = FXCollections.observableArrayList();

    @FXML
    private ComboBox<Customers> customerComboBox;

    @FXML
    private TextField productField;

    @FXML
    private TableView<CartItem> cartTable;

    @FXML
    private TableColumn<CartItem, String> productNameColumn;

    @FXML
    private TableColumn<CartItem, Double> quantityColumn;

    @FXML
    private TableColumn<CartItem, BigDecimal> totalPriceColumn;

    @FXML
    private Label totalLabel;

    @Autowired
    private CustomerRepository customerRepository;

    private final OrderRepository ordersRepository;

    private Customers selectedCustomer;

    private final BalancaController balancaController = BalancaController.getInstancia(); // ✅ CERTO


    public SalesController(ApplicationContext applicationContext, ProductsRepository productsRepository, OrderRepository ordersRepository) {
        this.applicationContext = applicationContext;
        this.productsRepository = productsRepository;
        this.ordersRepository = ordersRepository;
    }

    @FXML
    public void initialize() {
        // Configuração das colunas da tabela do carrinho
        productNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getProduct().getName()));

        quantityColumn.setCellValueFactory(cellData ->
                cellData.getValue().quantityProperty().asObject());

        totalPriceColumn.setCellValueFactory(cellData ->
                cellData.getValue().totalPriceProperty());

        cartTable.setItems(cart);
        cart.addListener((ListChangeListener<CartItem>) change -> updateTotal());

        List<Customers> customers = customerRepository.findAll();
        ObservableList<Customers> customerOptions = FXCollections.observableArrayList();

        Customers noCustomer = new Customers(0L, "Nenhum Cliente", "", "");
        customerOptions.add(noCustomer);
        customerOptions.addAll(customers);

        customerComboBox.setItems(customerOptions);

        customerComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldCustomer, newCustomer) -> {
            selectedCustomer = (newCustomer != null) ? newCustomer : noCustomer;
        });

        customerComboBox.getSelectionModel().select(noCustomer);
        selectedCustomer = noCustomer;

        VBox.setVgrow(cartTable, Priority.ALWAYS);
    }

    public void initializeData(String userInfo) {
        System.out.println("Initializing data with: " + userInfo);
    }

    private void updateTotal() {
        BigDecimal total = cart.stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        totalLabel.setText(String.format("Total: R$ %.2f", total));
    }

    public void addCartItem(Product product, double quantity) {
        if (product != null && quantity > 0) {
            // Verifica se o produto é do tipo "Unidade" e se a quantidade é válida
            if (!"Kg".equals(product.getTipo()) && quantity <= 0) {
                showAlert("Erro", "Quantidade inválida para produto por unidade.", Alert.AlertType.ERROR);
                return;
            }

            // Adiciona o item ao carrinho com a quantidade já informada (peso ou unidades)
            CartItem cartItem = new CartItem(null, product, quantity);
            cart.add(cartItem);
            cartTable.refresh();  // Atualiza a tabela do carrinho
            updateTotal();  // Atualiza o total do carrinho
        }
    }




    @FXML
    private void handleRemoveItem() {
        CartItem selectedItem = cartTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            cart.remove(selectedItem);
            cartTable.refresh();
            updateTotal();
        }
    }

    @FXML
    private void handleViewOrders() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/orders-view.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());

            Stage stage = new Stage();
            stage.setTitle("Pedidos Finalizados");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert("Erro", "Erro ao carregar a tela de pedidos finalizados.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleProductSearch() {
        String query = productField.getText().trim();

        if (query.isEmpty()) {
            showAlert("Atenção", "Por favor, insira o nome ou ID do produto.", Alert.AlertType.WARNING);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/SearchResultsView.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());

            SearchResultsController controller = loader.getController();

            Stage stage = new Stage();
            controller.setDialogStage(stage);
            controller.setSalesController(this);

            controller.initializeSearch(productsRepository.findAll());
            controller.searchProducts(query);

            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Resultados da Pesquisa");
            stage.setScene(scene);
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erro", "Não foi possível carregar a tela de resultados.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleAbrirCheckout() {
        if (cart.isEmpty()) {
            showAlert("Erro", "O carrinho está vazio!", Alert.AlertType.ERROR);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/CheckoutView.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());

            CheckoutController controller = loader.getController();

            controller.setCart(cart);
            controller.setCustomer(customerComboBox.getSelectionModel().getSelectedItem());
            controller.setValorTotal(cart.stream()
                    .map(CartItem::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .doubleValue());
            controller.setOrdersRepository(applicationContext.getBean(OrderRepository.class));
            controller.setProductsRepository(applicationContext.getBean(ProductsRepository.class));

            controller.setCompanyConfigRepository(applicationContext.getBean(CompanyConfigRepository.class));

            Stage stage = new Stage();
            stage.setTitle("Checkout");
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            showAlert("Erro", "Não foi possível abrir o checkout.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }


    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/DashBoardView.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());

            Stage stage = (Stage) productField.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert("Erro", "Não foi possível carregar a tela do Dashboard.", Alert.AlertType.ERROR);
            e.printStackTrace();
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
