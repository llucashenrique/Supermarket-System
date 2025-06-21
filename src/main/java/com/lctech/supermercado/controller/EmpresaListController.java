package com.lctech.supermercado.controller;

import com.lctech.supermercado.config.ApplicationContextProvider;
import com.lctech.supermercado.model.CompanyConfig;
import com.lctech.supermercado.repository.CompanyConfigRepository;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class EmpresaListController {

    @FXML private TableView<CompanyConfig> empresaTable;
    @FXML private TableColumn<CompanyConfig, String> colRazao;
    @FXML private TableColumn<CompanyConfig, String> colCnpj;
    @FXML private TableColumn<CompanyConfig, String> colEndereco;
    @FXML private TableColumn<CompanyConfig, String> colUf;
    @FXML private TableColumn<CompanyConfig, String> colCep;
    @FXML private TableColumn<CompanyConfig, String> colTelefone;
    @FXML private TableColumn<CompanyConfig, String> colEmail;
    @FXML private TableColumn<CompanyConfig, String> colIbge;
    @FXML private TableColumn<CompanyConfig, Void> colAcoes;
    @FXML private TableColumn<CompanyConfig, String> colMunicipio;



    private final CompanyConfigRepository repository =
            ApplicationContextProvider.getContext().getBean(CompanyConfigRepository.class);

    @FXML
    public void initialize() {
        colRazao.setCellValueFactory(new PropertyValueFactory<>("razaoSocial"));
        colCnpj.setCellValueFactory(new PropertyValueFactory<>("cnpj"));
        colEndereco.setCellValueFactory(new PropertyValueFactory<>("endereco"));
        colMunicipio.setCellValueFactory(new PropertyValueFactory<>("municipio"));
        colUf.setCellValueFactory(new PropertyValueFactory<>("uf"));
        colTelefone.setCellValueFactory(new PropertyValueFactory<>("telefone"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colCep.setCellValueFactory(new PropertyValueFactory<>("cep"));
        colIbge.setCellValueFactory(new PropertyValueFactory<>("codigoIbge"));
        colMunicipio.setCellValueFactory(new PropertyValueFactory<>("municipio")); // mesmo valor do campo "cidade"

        addButtonToTable();
        carregarEmpresas();
    }



    private void carregarEmpresas() {
        List<CompanyConfig> empresas = repository.findAll();
        empresaTable.getItems().setAll(empresas);
    }

    private void addButtonToTable() {
        colAcoes.setCellFactory(param -> new TableCell<>() {
            private final Button btnEditar = new Button("Editar");

            {
                btnEditar.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                btnEditar.setOnAction(event -> {
                    CompanyConfig empresa = getTableView().getItems().get(getIndex());
                    abrirTelaEdicao(empresa);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(new HBox(btnEditar));
                }
            }
        });
    }

    private void abrirTelaEdicao(CompanyConfig empresa) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/EmpresaConfigView.fxml"));
            Parent root = loader.load();

            EmpresaConfigController controller = loader.getController();
            controller.setCompanyConfigRepository(repository);
            controller.carregarEmpresaParaEdicao(empresa);

            Stage stage = new Stage();
            stage.setTitle("Editar Empresa");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            carregarEmpresas();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
