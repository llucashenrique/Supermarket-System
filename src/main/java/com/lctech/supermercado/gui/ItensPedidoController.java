package com.lctech.supermercado.gui;

import com.lctech.supermercado.model.CartItem;
import com.lctech.supermercado.model.Order;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;


@Component
public class ItensPedidoController {

    @FXML
    private TableView<CartItem> itensTable;
    @FXML
    private TableColumn<CartItem, String> produtoColumn;
    @FXML
    private TableColumn<CartItem, Double> quantidadeColumn;
    @FXML
    private TableColumn<CartItem, BigDecimal> precoColumn;
    @FXML
    private Label totalLabel;

    private final ObservableList<CartItem> itensList = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        produtoColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getProduct().getName())
        );

        quantidadeColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getQuantity())
        );

        precoColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getTotalPrice())
        );

        itensTable.setItems(itensList);
    }

    public void setPedido(Order pedido) {
        if (pedido != null && pedido.getItems() != null) {
            itensTable.setItems(FXCollections.observableArrayList(pedido.getItems()));

            double total = pedido.getItems().stream()
                    .mapToDouble(item -> item.getTotalPrice().doubleValue())
                    .sum();

            totalLabel.setText(String.format("Total: R$ %.2f", total));
        }
    }


}


