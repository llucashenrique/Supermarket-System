package com.lctech.supermercado.gui;

import com.lctech.supermercado.impressora.CupomGenerator;
import com.lctech.supermercado.impressora.ImpressoraElginUSB;
import com.lctech.supermercado.impressora.ImpressoraService;
import com.lctech.supermercado.model.*;
import com.lctech.supermercado.repository.CompanyConfigRepository;
import com.lctech.supermercado.repository.OrderRepository;
import com.lctech.supermercado.repository.ProductsRepository;
import com.lctech.supermercado.service.NfeService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.w3c.dom.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CheckoutController {

    @FXML
    private Label totalLabel;

    private String formaPagamentoSelecionada = "";

    @FXML
    private Button btnDinheiro;
    @FXML
    private Button btnDebito;
    @FXML
    private Button btnCredito;
    @FXML
    private Button btnPix;
    @FXML
    private ComboBox<String> formaPagamentoComboBox;

    private double valorTotal;
    private List<CartItem> cart;
    private Customers selectedCustomer;
    private OrderRepository ordersRepository;
    private CompanyConfigRepository companyConfigRepository;

    public void setCompanyConfigRepository(CompanyConfigRepository repository) {
        this.companyConfigRepository = repository;
    }

    public void setValorTotal(double valorTotal) {
        this.valorTotal = valorTotal;
        totalLabel.setText(String.format("R$ %.2f", valorTotal));
    }

    public void setOrdersRepository(OrderRepository ordersRepository) {
        this.ordersRepository = ordersRepository;
    }

    public void setCart(List<CartItem> cart) {
        this.cart = cart;
    }

    public void setCustomer(Customers customer) {
        this.selectedCustomer = customer;
    }

    private ProductsRepository productsRepository;

    public void setProductsRepository(ProductsRepository productsRepository) {
        this.productsRepository = productsRepository;
    }
    @FXML
    private void handleConfirmarPagamento() {
        if (formaPagamentoSelecionada == null || formaPagamentoSelecionada.isEmpty()) {
            showAlert("Atenção", "Selecione uma forma de pagamento.");
            return;
        }

        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmação");
        confirmacao.setHeaderText("Finalizar Venda");
        confirmacao.setContentText("Deseja realmente finalizar a venda com pagamento via " + formaPagamentoSelecionada + "?");

        confirmacao.showAndWait().ifPresent(response -> {
            if (response.getButtonData().isDefaultButton()) {
                try {
                    Order order = new Order();
                    order.setOrderDate(LocalDateTime.now());

                    if (selectedCustomer == null || selectedCustomer.getId() == 0L) {
                        order.setCustomer(null);
                    } else {
                        order.setCustomer(selectedCustomer);
                    }

                    List<CartItem> novosItens = new ArrayList<>();
                    for (CartItem item : cart) {
                        CartItem novo = new CartItem();
                        novo.setOrder(order);
                        novo.setProduct(item.getProduct());
                        novo.setQuantity(item.getQuantity());
                        novo.setTotalPrice(item.getTotalPrice());
                        novosItens.add(novo);
                    }
                    order.setItems(novosItens);

                    order.setQuantity(novosItens.stream()
                            .mapToDouble(CartItem::getQuantity)
                            .sum());
                    order.setTotalPrice(novosItens.stream()
                            .map(CartItem::getTotalPrice)
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .doubleValue());

                    order.setPaymentType(formaPagamentoSelecionada.toUpperCase());
                    ordersRepository.save(order);

                    for (CartItem item : novosItens) {
                        Product product = item.getProduct();
                        double novoEstoque = product.getStock() - item.getQuantity();

                        if (novoEstoque < 0) {
                            showAlert("Erro", "Estoque insuficiente para o produto: " + product.getName());
                            return;
                        }

                        product.setStock(novoEstoque);
                        productsRepository.save(product);
                    }

                    TextInputDialog docDialog = new TextInputDialog();
                    docDialog.setTitle("Cupom Fiscal");
                    docDialog.setHeaderText("Deseja informar CPF ou CNPJ?");
                    docDialog.setContentText("Digite o CPF/CNPJ (ou 0 para não emitir cupom fiscal):");

                    Optional<String> docResult = docDialog.showAndWait();
                    final boolean[] emitirNfce = {true};

                    if (docResult.isPresent()) {
                        String documento = docResult.get().trim();

                        if (documento.equals("0")) {
                            emitirNfce[0] = false;
                        } else {
                            if (!documento.isEmpty()) {
                                if (!documento.matches("\\d{11}|\\d{14}")) {
                                    showAlert("Erro", "CPF/CNPJ inválido. Use 11 ou 14 dígitos.", Alert.AlertType.ERROR);
                                    return;
                                } else {
                                    order.setDocumentoConsumidor(documento);
                                    ordersRepository.save(order);
                                }
                            }
                        }
                    }

                    cart.clear();

                    Alert impressao = new Alert(Alert.AlertType.CONFIRMATION);
                    impressao.setTitle("Impressão de Cupom");
                    impressao.setHeaderText("Deseja imprimir o cupom da venda?");
                    impressao.setContentText("Forma de pagamento: " + formaPagamentoSelecionada);

                    impressao.showAndWait().ifPresent(resp -> {
                        if (resp.getButtonData().isDefaultButton()) {
                            try {
                                CompanyConfig empresa = companyConfigRepository.findById(1L).orElseThrow();

                                if (emitirNfce[0]) {
                                    NfeService nfeService = new NfeService();
                                    nfeService.carregarCertificado(empresa.getCertificadoPath(), empresa.getSenhaCertificado());

                                    // Emissão da NFC-e
                                    Document xml = nfeService.gerarXmlNFCe(empresa);

                                    String idCompleto = xml.getElementsByTagName("infNFe").item(0)
                                            .getAttributes().getNamedItem("Id")
                                            .getNodeValue(); // já inclui "NFe"

                                    order.setChave(idCompleto.replace("NFe", ""));
                                    ordersRepository.save(order);

                                    System.out.println("🔐 Chave gerada para a nota: " + idCompleto);
                                    System.out.println("🔐 Chave no pedido: " + order.getChave());

                                    Document assinado = nfeService.assinarXml(xml, "infNFe", idCompleto);
                                    nfeService.enviarParaSefaz(assinado, empresa);

                                    String chaveAcesso = idCompleto;
                                    String urlConsulta = "https://www.homologacao.nfce.fazenda.sp.gov.br/consulta";

                                    CupomGenerator generator = new CupomGenerator();
                                    String danfe = generator.gerarDanfeNfce(empresa, order, chaveAcesso, urlConsulta);
                                    ImpressoraService impressora = new ImpressoraElginUSB(empresa, order);
                                    impressora.imprimir(danfe);

                                    showAlert("Sucesso", "NFC-e emitida com sucesso!", Alert.AlertType.INFORMATION);

                                } else {
                                    CupomGenerator generator = new CupomGenerator();
                                    String conteudo = generator.gerarCupom(empresa, order, order.getId().intValue());
                                    ImpressoraService impressora = new ImpressoraElginUSB(empresa, order);
                                    impressora.imprimir(conteudo);
                                }

                            } catch (Exception ex) {
                                ex.printStackTrace();
                                showAlert("Erro", "Erro ao imprimir cupom: " + ex.getMessage());
                            }
                        }
                    });

                    showAlert("Sucesso", "Pagamento realizado com sucesso via " + formaPagamentoSelecionada + "!");

                    Stage stage = (Stage) totalLabel.getScene().getWindow();
                    stage.close();

                } catch (Exception e) {
                    showAlert("Erro", "Erro ao registrar venda: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML
    public void initialize() {
        totalLabel.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.setOnKeyPressed(event -> {
                    switch (event.getCode()) {
                        case F1 -> handlePagamentoDinheiro();
                        case F2 -> handlePagamentoDebito();
                        case F3 -> handlePagamentoCredito();
                        case F4 -> handlePagamentoPix();
                    }
                });
            }
        });
    }

    @FXML
    private void handlePagamentoDinheiro() {
        formaPagamentoSelecionada = "Dinheiro";
        highlightSelecionado(btnDinheiro);
    }

    @FXML
    private void handlePagamentoDebito() {
        formaPagamentoSelecionada = "Débito";
        highlightSelecionado(btnDebito);
    }

    @FXML
    private void handlePagamentoCredito() {
        formaPagamentoSelecionada = "Crédito";
        highlightSelecionado(btnCredito);
    }

    @FXML
    private void handlePagamentoPix() {
        formaPagamentoSelecionada = "Pix";
        highlightSelecionado(btnPix);
    }

    // Versão completa com tipo de alerta
    private void showAlert(String titulo, String mensagem, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    // Versão simplificada (padrão: INFORMATION)
    private void showAlert(String titulo, String mensagem) {
        showAlert(titulo, mensagem, Alert.AlertType.INFORMATION);
    }


    private void highlightSelecionado() {
        System.out.println("Pagamento selecionado: " + formaPagamentoSelecionada);

    }

    private void highlightSelecionado(Button selectedButton) {
        // Resetar todos os estilos
        btnDinheiro.setStyle(estiloPadrao(btnDinheiro.getText()));
        btnDebito.setStyle(estiloPadrao(btnDebito.getText()));
        btnCredito.setStyle(estiloPadrao(btnCredito.getText()));
        btnPix.setStyle(estiloPadrao(btnPix.getText()));

        // Destacar o botão clicado
        selectedButton.setStyle("-fx-background-color: #264653; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 10; -fx-border-color: #000; -fx-border-width: 2;");
    }

    private String estiloPadrao(String label) {
        if (label.contains("Dinheiro")) {
            return "-fx-background-color: #2a9d8f; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 10;";
        } else if (label.contains("Débito")) {
            return "-fx-background-color: #e9c46a; -fx-text-fill: #333; -fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 10;";
        } else if (label.contains("Crédito")) {
            return "-fx-background-color: #f4a261; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 10;";
        } else if (label.contains("Pix")) {
            return "-fx-background-color: #e76f51; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 10;";
        }
        return "";
    }

}
