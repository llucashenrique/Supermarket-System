package com.lctech.supermercado.gui;

import com.lctech.supermercado.model.Customers;
import com.lctech.supermercado.model.Order;
import com.lctech.supermercado.model.PedidoWrapper;
import com.lctech.supermercado.repository.CustomerRepository;
import com.lctech.supermercado.repository.OrderRepository;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class ClienteContaController {

    @FXML
    private ComboBox<Customers> clienteComboBox;
    @FXML
    private TableView<PedidoWrapper> pedidosTable;
    @FXML
    private TableColumn<PedidoWrapper, Boolean> selecionadoColumn;
    @FXML
    private TableColumn<PedidoWrapper, Long> pedidoIdColumn;
    @FXML
    private TableColumn<PedidoWrapper, LocalDateTime> dataColumn;
    @FXML
    private TableColumn<PedidoWrapper, Double> quantidadeColumn;
    @FXML
    private TableColumn<PedidoWrapper, Double> totalColumn;
    @FXML
    private TableColumn<PedidoWrapper, String> statusColumn;
    @FXML
    private TableColumn<PedidoWrapper, LocalDateTime> dataPagamentoColumn;
    @FXML
    private Label totalFiadoLabel;

    @FXML
    private Label totalSelecionadoLabel;


    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final ApplicationContext applicationContext;

    private final ObservableList<PedidoWrapper> pedidosWrapperList = FXCollections.observableArrayList();

    @Autowired
    public ClienteContaController(CustomerRepository customerRepository,
                                  OrderRepository orderRepository,
                                  ApplicationContext applicationContext) {
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
        this.applicationContext = applicationContext;
    }

    @FXML
    public void initialize() {
        configureTableColumns();

        List<Customers> clientes = customerRepository.findAll();
        clienteComboBox.setItems(FXCollections.observableArrayList(clientes));

        clienteComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                carregarPedidosDoCliente(newVal);
            }
        });

        pedidosTable.setEditable(true);
        selecionadoColumn.setEditable(true);

        pedidosWrapperList.addListener((ListChangeListener<PedidoWrapper>) change -> {
            atualizarTotalSelecionado();
        });

        pedidosWrapperList.forEach(wrapper ->
                wrapper.selecionadoProperty().addListener((obs, oldVal, newVal) -> atualizarTotalSelecionado())
        );


    }

    private void configureTableColumns() {
        selecionadoColumn.setCellValueFactory(param -> param.getValue().selecionadoProperty());
        selecionadoColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selecionadoColumn));

        pedidoIdColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getPedido().getId()));
        dataColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getPedido().getOrderDate()));
        quantidadeColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getPedido().getQuantity()));
        totalColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getPedido().getTotalPrice()));
        statusColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getPedido().isPago() ? "Pago" : "Pendente"));
        dataPagamentoColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getPedido().getDataPagamento()));
    }

    private void carregarPedidosDoCliente(Customers cliente) {
        List<Order> pedidos = orderRepository.findByCustomer(cliente);
        pedidosWrapperList.clear();
        pedidos.forEach(pedido -> pedidosWrapperList.add(new PedidoWrapper(pedido)));

        pedidosTable.setItems(pedidosWrapperList);

        for (PedidoWrapper wrapper : pedidosWrapperList) {
            wrapper.selecionadoProperty().addListener((obs, oldVal, newVal) -> atualizarTotalSelecionado());
        }


        double total = pedidos.stream()
                .mapToDouble(Order::getTotalPrice)
                .sum();

        totalFiadoLabel.setText(
                pedidos.isEmpty()
                        ? "Este cliente não possui pedidos."
                        : "Total em aberto: R$ " + String.format("%.2f", total)
        );

        atualizarTotalSelecionado();
        atualizarTotalEmAberto(); // <- novo aqui também

    }

    @FXML
    private void handlePagamento() {
        double totalPago = 0;

        for (PedidoWrapper wrapper : pedidosWrapperList) {
            Order pedido = wrapper.getPedido();
            if (wrapper.isSelecionado() && !pedido.isPago()) {
                pedido.setPago(true);
                pedido.setDataPagamento(LocalDateTime.now());
                orderRepository.save(pedido);
                totalPago += pedido.getTotalPrice();
            }
        }

        Customers clienteSelecionado = clienteComboBox.getSelectionModel().getSelectedItem();
        if (clienteSelecionado != null) {
            carregarPedidosDoCliente(clienteSelecionado);
        }

        pedidosTable.refresh();
        atualizarTotalSelecionado();   // Atualiza o total selecionado
        atualizarTotalEmAberto();      // Atualiza o total em aberto
        showAlert("Pagamento realizado", "Foram pagos R$ " + String.format("%.2f", totalPago), Alert.AlertType.ERROR);

    }

    private void atualizarTotalEmAberto() {
        double totalAberto = pedidosWrapperList.stream()
                .filter(wrapper -> !wrapper.getPedido().isPago())
                .mapToDouble(wrapper -> wrapper.getPedido().getTotalPrice())
                .sum();

        totalFiadoLabel.setText(
                pedidosWrapperList.isEmpty()
                        ? "Este cliente não possui pedidos."
                        : "Total em aberto: R$ " + String.format("%.2f", totalAberto)
        );
    }


    private void atualizarTotalSelecionado() {
        double totalSelecionado = pedidosWrapperList.stream()
                .filter(PedidoWrapper::isSelecionado)
                .filter(wrapper -> !wrapper.getPedido().isPago())
                .mapToDouble(wrapper -> wrapper.getPedido().getTotalPrice())
                .sum();

        totalSelecionadoLabel.setText("Total selecionado: R$ " + String.format("%.2f", totalSelecionado));
    }

    @FXML
    private void handleViewOrderDetails() {
        PedidoWrapper selectedWrapper = pedidosTable.getSelectionModel().getSelectedItem();

        if (selectedWrapper != null) {
            Long idPedido = selectedWrapper.getPedido().getId();

            // 🔥 Aqui estamos usando o método com fetch dos itens!
            Order selectedOrder = orderRepository.buscarComItens(idPedido);

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ItensPedidoView.fxml"));
                loader.setControllerFactory(applicationContext::getBean);
                Parent root = loader.load();

                ItensPedidoController controller = loader.getController();
                controller.setPedido(selectedOrder);

                Stage stage = new Stage();
                stage.setTitle("Itens do Pedido");
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                showAlert("Erro", "Não foi possível abrir os detalhes do pedido.", Alert.AlertType.ERROR);
            }

        } else {
            showAlert("Atenção", "Selecione um pedido para ver os detalhes.", Alert.AlertType.WARNING);
        }
    }




    @FXML
    private void handleBack(ActionEvent event) {
        try {
            MenuItem item = (MenuItem) event.getSource();
            ContextMenu menu = item.getParentPopup();
            Node ownerNode = menu.getOwnerNode();
            Stage stage = (Stage) ownerNode.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            showAlert("Erro", "Não foi possível fechar a janela.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void showAlert(String titulo, String mensagem, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }


}

