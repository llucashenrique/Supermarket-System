package com.lctech.supermercado.gui;

import com.lctech.supermercado.model.CartItem;
import com.lctech.supermercado.model.Order;
import com.lctech.supermercado.repository.OrderRepository;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class OrdersController {

    private final ApplicationContext applicationContext;
    private final OrderRepository ordersRepository;
    private final ObservableList<Order> ordersList = FXCollections.observableArrayList();
    private final ObservableList<CartItem> cartItemsList = FXCollections.observableArrayList();

    @FXML
    private Label loadingLabel;

    @FXML
    private TableView<Order> ordersTable;

    @FXML
    private TableColumn<Order, Long> orderIdColumn;


    @FXML
    private TableColumn<Order, LocalDateTime> orderDateColumn;

    @FXML
    private TableView<CartItem> cartTable;

    @FXML
    private TableColumn<CartItem, String> productColumn;

    @FXML
    private TableColumn<CartItem, Double> quantityColumn;

    @FXML
    private TableColumn<Order, BigDecimal> orderTotalColumn;

    @FXML
    private TableColumn<CartItem, BigDecimal> totalPriceColumn;

    @FXML
    private TableColumn<CartItem, String> customerNameColumn;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private Label totalOrderLabel;

    public OrdersController(ApplicationContext applicationContext, OrderRepository ordersRepository) {
        this.applicationContext = applicationContext;
        this.ordersRepository = ordersRepository;
    }

    @FXML
    public void initialize() {
        orderIdColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getId()));


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        orderDateColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getOrderDate()));

        orderDateColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(formatter));
                }
            }
        });

        quantityColumn.setCellValueFactory(cellData ->
                cellData.getValue().quantityProperty().asObject());


        productColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getName()));
        totalPriceColumn.setCellValueFactory(cellData ->
                cellData.getValue().totalPriceProperty());
        ordersTable.setItems(ordersList);
        cartTable.setItems(cartItemsList);
        loadOrders();

        orderTotalColumn.setCellValueFactory(cellData ->
                cellData.getValue().totalAmountProperty());

        customerNameColumn.setCellValueFactory(cellData -> {
            Order order = cellData.getValue().getOrder();
            if (order != null && order.getCustomer() != null) {
                return new SimpleStringProperty(order.getCustomer().getName());
            } else {
                return new SimpleStringProperty("Nenhum Cliente");
            }
        });

    }
    @FXML
    private void handleFiltrarPedidos() {
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();

        if (start == null || end == null || end.isBefore(start)) {
            showAlert("Alerta de Data", "Selecione um intervalo de datas válido.", Alert.AlertType.WARNING);
            return;
        }

        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.plusDays(1).atStartOfDay();

        List<Order> todas = ordersRepository.findAllWithItems();
        List<Order> filtradas = todas.stream()
                .filter(order -> order.getOrderDate() != null
                        && !order.getOrderDate().isBefore(startDateTime)
                        && order.getOrderDate().isBefore(endDateTime))
                .collect(Collectors.toList());

        ordersList.setAll(filtradas);
        ordersTable.refresh();
        cartItemsList.clear();
        totalOrderLabel.setText("Total: R$ 0.00");
    }

    @FXML
    private void handleViewOrderDetails() {
        Order selectedOrder = ordersTable.getSelectionModel().getSelectedItem();
        if (selectedOrder != null) {
            cartItemsList.setAll(selectedOrder.getItems());
            cartTable.refresh();

            double total = selectedOrder.getItems().stream()
                    .mapToDouble(cartItem -> cartItem.getTotalPrice().doubleValue())
                    .sum();

            totalOrderLabel.setText(String.format("Total: R$ %.2f", total));
        } else {
            showAlert("Atenção", "Selecione um pedido para ver os detalhes.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void handleRemoveOrder() {
        Order selectedOrder = ordersTable.getSelectionModel().getSelectedItem();
        if (selectedOrder != null) {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Remover Pedido");
            confirmation.setHeaderText(null);
            confirmation.setContentText("Tem certeza que deseja remover este pedido?");

            confirmation.showAndWait().ifPresent(response -> {
                if (response.getButtonData().isDefaultButton()) {
                    try {
                        ordersRepository.delete(selectedOrder);
                        ordersList.remove(selectedOrder);
                        ordersTable.refresh();
                        showAlert("Sucesso", "Pedido removido com sucesso.", Alert.AlertType.INFORMATION);
                    } catch (Exception e) {
                        showAlert("Erro", "Erro ao remover pedido: " + e.getMessage(), Alert.AlertType.ERROR);
                    }
                }
            });
        } else {
            showAlert("Atenção", "Selecione um pedido para remover.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void handleKeyPress(javafx.scene.input.KeyEvent event) {
        if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
            ordersTable.getScene().getWindow().hide();
        }
    }


    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void loadOrders() {
        List<Order> orders = ordersRepository.findAllWithItems();
        ordersList.setAll(orders);
    }
}
