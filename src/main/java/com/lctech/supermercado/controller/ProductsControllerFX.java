package com.lctech.supermercado.controller;

import com.lctech.supermercado.dto.IbptResponse;
import com.lctech.supermercado.model.Product;
import com.lctech.supermercado.repository.ProductsRepository;
import com.lctech.supermercado.service.FiscalApiService;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Component
public class ProductsControllerFX {

    private final ProductsRepository productsRepository;
    private final ApplicationContext applicationContext;

    public ProductsControllerFX(ProductsRepository productsRepository, ApplicationContext applicationContext) {
        this.productsRepository = productsRepository;
        this.applicationContext = applicationContext;
    }

    @FXML private TextField productNameField;
    @FXML private TextField productPriceField;
    @FXML private TextField productStockField;
    @FXML private TextField productBarcodeField;
    @FXML private TextField productNcmField;
    @FXML private TextField searchField;
    @FXML private ListView<String> productSuggestions;
    @FXML private ChoiceBox<String> productTypeChoiceBox;
    @FXML private CheckBox balancaCheckBox;

    // Campos de Tributação
    @FXML private TextField origemField;
    @FXML private TextField cstIcmsField;
    @FXML private TextField aliquotaIcmsField;
    @FXML private TextField cstPisField;
    @FXML private TextField aliquotaPisField;
    @FXML private TextField cstCofinsField;
    @FXML private TextField aliquotaCofinsField;

    // Outros campos
    @FXML private TextField cestField;
    @FXML private CheckBox incluirNoTotalCheckBox;

    @Autowired
    private FiscalApiService fiscalApiService;

    private Product selectedProduct;

    @FXML
    public void initialize() {
        // Busca dinâmica ao digitar
        searchField.textProperty().addListener((obs, oldVal, newVal) -> searchProducts(newVal));

        productSuggestions.setOnMouseClicked(evt -> {
            if (evt.getClickCount() == 2) {
                String sel = productSuggestions.getSelectionModel().getSelectedItem();
                if (sel != null) setSelectedProduct(sel);
            }
        });

        productSuggestions.setOnKeyPressed(evt -> {
            switch (evt.getCode()) {
                case ENTER -> {
                    String sel = productSuggestions.getSelectionModel().getSelectedItem();
                    if (sel != null) setSelectedProduct(sel);
                }
                case UP, DOWN -> {
                    if (!productSuggestions.getItems().isEmpty())
                        productSuggestions.requestFocus();
                }
            }
        });

        searchField.setOnKeyPressed(evt -> {
            if (evt.getCode() == javafx.scene.input.KeyCode.DOWN
                    && !productSuggestions.getItems().isEmpty()) {
                productSuggestions.requestFocus();
                productSuggestions.getSelectionModel().select(0);
            }
        });

        productTypeChoiceBox.setItems(FXCollections.observableArrayList("Kg", "Unidade", "Litro", "Pacote"));

        // Preenchimento automático via API IBPT ao sair do campo NCM
        productNcmField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (wasFocused && !isNowFocused) {
                String ncm = productNcmField.getText().trim();
                if (ncm.length() == 8) {
                    try {
                        IbptResponse resp = fiscalApiService.lookupByNcm(ncm);

                        BigDecimal icmsAliq = resp.getSituacao_tributaria().getIcms().getAliquota();
                        BigDecimal pisAliq = resp.getSituacao_tributaria().getPis().getAliquota();
                        BigDecimal cofinsAliq = resp.getSituacao_tributaria().getCofins().getAliquota();

                        origemField.setText("0");
                        cstIcmsField.setText("102");
                        aliquotaIcmsField.setText(icmsAliq.toString());
                        cstPisField.setText("01");
                        aliquotaPisField.setText(pisAliq.toString());
                        cstCofinsField.setText("01");
                        aliquotaCofinsField.setText(cofinsAliq.toString());

                        cestField.clear();

                    } catch (ResourceAccessException rae) {
                        showAlert("Aviso", "Não foi possível acessar a API fiscal. Verifique sua conexão.",
                                Alert.AlertType.WARNING);
                    } catch (Exception ex) {
                        showAlert("Erro", "Erro ao buscar dados fiscais: " + ex.getMessage(),
                                Alert.AlertType.ERROR);
                    }
                }
            }
        });
    }

    private void searchProducts(String query) {
        if (query.isEmpty()) {
            productSuggestions.getItems().clear();
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

            productSuggestions.getItems().clear();
            products.forEach(product -> productSuggestions.getItems().add(
                    String.format("%s (ID: %d) - R$ %.2f", product.getName(), product.getId(), product.getPrice())
            ));
        } catch (Exception e) {
            showAlert("Erro", "Erro ao buscar produtos: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void setSelectedProduct(String selectedItem) {
        try {
            // Ex: "Arroz (ID: 15) - R$ 5,00"
            String[] parts = selectedItem.split(" \\(ID: ");
            if (parts.length < 2) return;  // formato inesperado

            String[] tail = parts[1].split("\\)");
            if (tail.length < 1) return;

            Long id = Long.parseLong(tail[0].trim());
            Product prod = productsRepository.findById(id).orElse(null);
            if (prod != null) {
                this.selectedProduct = prod;
                setSelectedProduct(prod);
            }
        } catch (NumberFormatException e) {
            showAlert("Erro", "ID de produto inválido.", Alert.AlertType.ERROR);
        } catch (Exception e) {
            showAlert("Erro", "Erro ao selecionar o produto: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }


    public void setSelectedProduct(Product product) {
        if (product == null) return;
        this.selectedProduct = product;

        // Identificação
        productNameField.setText(product.getName());
        productPriceField.setText(product.getPrice().toString());
        productStockField.setText(String.valueOf(product.getStock()));
        productBarcodeField.setText(product.getBarcode());
        productTypeChoiceBox.setValue(product.getTipo());
        productNcmField.setText(product.getNcm());
        balancaCheckBox.setSelected(product.isBalanca());

        // Tributação
        origemField.setText(product.getOrigem());
        cstIcmsField.setText(product.getCstIcms());
        aliquotaIcmsField.setText(
                product.getAliquotaIcms() != null ? product.getAliquotaIcms().toString() : ""
        );
        cstPisField.setText(product.getCstPis());
        aliquotaPisField.setText(
                product.getAliquotaPis() != null ? product.getAliquotaPis().toString() : ""
        );
        cstCofinsField.setText(product.getCstCofins());
        aliquotaCofinsField.setText(
                product.getAliquotaCofins() != null ? product.getAliquotaCofins().toString() : ""
        );

        // Outros
        cestField.setText(product.getCest());
        incluirNoTotalCheckBox.setSelected(Boolean.TRUE.equals(product.getIncluirNoTotal()));
    }

    @FXML
    private void handleSaveChanges() {
        if (selectedProduct == null) {
            showAlert("Erro", "Nenhum produto selecionado para edição.", Alert.AlertType.ERROR);
            return;
        }

        try {
            String name = productNameField.getText().trim();
            BigDecimal price = new BigDecimal(productPriceField.getText().trim());
            double stock = Double.parseDouble(productStockField.getText().trim());
            String barcode = productBarcodeField.getText().trim();
            if (!barcode.isEmpty() && !barcode.matches("\\d{8,13}")) {
                showAlert("Erro", "Se informado, o código de barras deve conter entre 8 e 13 dígitos numéricos.", Alert.AlertType.ERROR);
                return;
            }
            String tipo = productTypeChoiceBox.getValue();
            String ncm = productNcmField.getText().trim();

            selectedProduct.setName(name);
            selectedProduct.setPrice(price);
            selectedProduct.setStock(stock);
            selectedProduct.setBarcode(barcode.isEmpty() ? null : barcode);
            selectedProduct.setTipo(tipo);
            selectedProduct.setNcm(ncm);
            selectedProduct.setBalanca(balancaCheckBox.isSelected());

            // dados fiscais
            selectedProduct.setOrigem(origemField.getText().trim());
            selectedProduct.setCstIcms(cstIcmsField.getText().trim());
            selectedProduct.setAliquotaIcms(new BigDecimal(aliquotaIcmsField.getText().trim()));
            selectedProduct.setCstPis(cstPisField.getText().trim());
            selectedProduct.setAliquotaPis(new BigDecimal(aliquotaPisField.getText().trim()));
            selectedProduct.setCstCofins(cstCofinsField.getText().trim());
            selectedProduct.setAliquotaCofins(new BigDecimal(aliquotaCofinsField.getText().trim()));

            // lógica para CEST
            String cestInput = cestField.getText().trim();
            if (cestInput.isEmpty()) {
                selectedProduct.setCest("ISENTO");
            } else {
                selectedProduct.setCest(cestInput);
            }

            selectedProduct.setIncluirNoTotal(incluirNoTotalCheckBox.isSelected());

            productsRepository.save(selectedProduct);
            showAlert("Sucesso", "Produto atualizado com sucesso!", Alert.AlertType.INFORMATION);
            clearFields();
        } catch (NumberFormatException e) {
            showAlert("Erro", "Valores inválidos para preço, estoque ou quantidade.", Alert.AlertType.ERROR);
        } catch (Exception e) {
            showAlert("Erro", "Erro ao atualizar o produto: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddProduct() {
        // Verifica se campos obrigatórios estão vazios
        if (isAnyFieldEmpty()
                || origemField.getText().trim().isEmpty()
                || cstIcmsField.getText().trim().isEmpty()
                || aliquotaIcmsField.getText().trim().isEmpty()
                || cstPisField.getText().trim().isEmpty()
                || aliquotaPisField.getText().trim().isEmpty()
                || cstCofinsField.getText().trim().isEmpty()
                || aliquotaCofinsField.getText().trim().isEmpty()) {
            showAlert("Erro", "Todos os campos (inclusive fiscais) devem ser preenchidos.", Alert.AlertType.ERROR);
            return;
        }

        try {
            Product product = new Product(
                    productNameField.getText().trim(),
                    productBarcodeField.getText().trim(),
                    new BigDecimal(productPriceField.getText().trim()),
                    Double.parseDouble(productStockField.getText().trim()),
                    productNcmField.getText().trim(),
                    productTypeChoiceBox.getValue(),
                    balancaCheckBox.isSelected()
            );

            // Fiscais
            product.setOrigem(origemField.getText().trim());
            product.setCstIcms(cstIcmsField.getText().trim());
            product.setAliquotaIcms(new BigDecimal(aliquotaIcmsField.getText().trim()));
            product.setCstPis(cstPisField.getText().trim());
            product.setAliquotaPis(new BigDecimal(aliquotaPisField.getText().trim()));
            product.setCstCofins(cstCofinsField.getText().trim());
            product.setAliquotaCofins(new BigDecimal(aliquotaCofinsField.getText().trim()));

            // Tratamento do CEST
            String cestInput = cestField.getText().trim();
            if (cestInput.isEmpty()) {
                product.setCest("ISENTO");
            } else {
                product.setCest(cestInput);
            }

            product.setIncluirNoTotal(incluirNoTotalCheckBox.isSelected());

            productsRepository.save(product);
            showAlert("Sucesso", "Produto adicionado com sucesso!", Alert.AlertType.INFORMATION);
            clearFields();
        } catch (NumberFormatException e) {
            showAlert("Erro", "Valores inválidos em algum campo numérico.", Alert.AlertType.ERROR);
        } catch (Exception e) {
            showAlert("Erro", "Erro ao adicionar o produto: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }


    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/DashBoardView.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            Stage stage = (Stage) productNameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Dashboard");
            stage.show();
        } catch (IOException e) {
            showAlert("Erro", "Não foi possível carregar a tela do Dashboard.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }


    private void clearFields() {
        productNameField.clear();
        productPriceField.clear();
        productStockField.clear();
        productBarcodeField.clear();
        productNcmField.clear();
        searchField.clear();
        productSuggestions.getItems().clear();
        productTypeChoiceBox.getSelectionModel().clearSelection();
        balancaCheckBox.setSelected(false);
        selectedProduct = null;
        origemField.clear();
        cstIcmsField.clear();
        aliquotaIcmsField.clear();
        cstPisField.clear();
        aliquotaPisField.clear();
        cstCofinsField.clear();
        aliquotaCofinsField.clear();
        cestField.clear();
        incluirNoTotalCheckBox.setSelected(false);
    }

    private boolean isAnyFieldEmpty() {
        return productNameField.getText().trim().isEmpty() ||
                productBarcodeField.getText().trim().isEmpty() ||
                productPriceField.getText().trim().isEmpty() ||
                productStockField.getText().trim().isEmpty() ||
                productNcmField.getText().trim().isEmpty();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
