package com.lctech.supermercado.controller;

import com.lctech.supermercado.dto.EmpresaDTO;
import com.lctech.supermercado.model.CompanyConfig;
import com.lctech.supermercado.model.Customers;
import com.lctech.supermercado.repository.CompanyConfigRepository;
import com.lctech.supermercado.service.CnpjService;
import com.lctech.supermercado.service.EmpresaMapper;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;

public class EmpresaConfigController {

    @FXML private TextField cnpjField;
    @FXML private TextField nomeFantasiaField;
    @FXML private TextField razaoSocialField;
    @FXML private TextField enderecoField;
    @FXML private TextField municipioField;
    @FXML private TextField ufField;
    @FXML private TextField cepField;
    @FXML private TextField telefoneField;
    @FXML private TextField emailField;
    @FXML private TextField codigoIbgeField;
    @FXML private TextField ieField;
    @FXML private TextField regimeField;
    @FXML private TextField certificadoPathField;
    @FXML private PasswordField senhaCertificadoField;
    @FXML private TextField xmlPathField;

    private CompanyConfigRepository companyConfigRepository;
    private CompanyConfig empresaAtual;
    private final CnpjService cnpjService = new CnpjService();

    public void setCompanyConfigRepository(CompanyConfigRepository repository) {
        this.companyConfigRepository = repository;
    }

    public void carregarEmpresaParaEdicao(CompanyConfig empresa) {
        this.empresaAtual = empresa;

        cnpjField.setText(empresa.getCnpj());
        razaoSocialField.setText(empresa.getRazaoSocial());
        nomeFantasiaField.setText(empresa.getNomeFantasia());
        enderecoField.setText(empresa.getEndereco());
        municipioField.setText(empresa.getMunicipio());
        ufField.setText(empresa.getUf());
        cepField.setText(empresa.getCep());
        telefoneField.setText(empresa.getTelefone());
        emailField.setText(empresa.getEmail());
        codigoIbgeField.setText(empresa.getCodigoIbge());
        ieField.setText(empresa.getInscricaoEstadual());
        regimeField.setText(empresa.getRegimeTributario());
        certificadoPathField.setText(empresa.getCertificadoPath());
        senhaCertificadoField.setText(empresa.getSenhaCertificado());
        xmlPathField.setText(empresa.getXmlPath());
    }

    @FXML
    private void handleBuscarEmpresaPorCnpj(ActionEvent event) {
        try {
            String cnpj = cnpjField.getText().trim();

            if (cnpj.isEmpty() || !cnpj.matches("\\d{14}")) {
                showAlert("Erro", "CNPJ inválido. Insira um CNPJ com 14 dígitos.", Alert.AlertType.ERROR);
                return;
            }

            Optional<Customers> optionalEmpresa = cnpjService.fetchCustomerByCnpj(cnpj);

            if (optionalEmpresa.isPresent()) {
                EmpresaDTO empresa = EmpresaMapper.fromCustomer(optionalEmpresa.get());
                preencherTelaComEmpresa(empresa);
                showAlert("Sucesso", "Dados carregados com sucesso!", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Erro", "Nenhuma empresa encontrada com esse CNPJ.", Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            showAlert("Erro", "Erro ao buscar dados: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void preencherTelaComEmpresa(EmpresaDTO empresa) {
        razaoSocialField.setText(empresa.getRazaoSocial());
        nomeFantasiaField.setText(empresa.getNomeFantasia());
        enderecoField.setText(empresa.getEndereco());
        municipioField.setText(empresa.getMunicipio());
        ufField.setText(empresa.getUf());
        cepField.setText(empresa.getCep());
        telefoneField.setText(empresa.getTelefone());
        emailField.setText(empresa.getEmail());
        codigoIbgeField.setText(empresa.getCodigoIbge());

        if (empresa.getInscricaoEstadual() != null)
            ieField.setText(empresa.getInscricaoEstadual());

        if (empresa.getRegimeTributario() != null)
            regimeField.setText(empresa.getRegimeTributario());
    }

    @FXML
    private void selecionarCertificado() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecionar Certificado Digital (.pfx ou .p12)");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Certificados (*.pfx, *.p12)", "*.pfx", "*.p12")
        );

        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            certificadoPathField.setText(file.getAbsolutePath());
        }
    }

    @FXML
    private void selecionarDiretorioXml() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Selecionar Diretório para salvar os XMLs");

        File selectedDirectory = directoryChooser.showDialog(null);
        if (selectedDirectory != null) {
            xmlPathField.setText(selectedDirectory.getAbsolutePath());
        }
    }

    @FXML
    private void salvarConfiguracoes() {
        CompanyConfig config = (empresaAtual != null) ? empresaAtual : new CompanyConfig();

        config.setCnpj(cnpjField.getText());
        config.setRazaoSocial(razaoSocialField.getText());
        config.setNomeFantasia(nomeFantasiaField.getText());
        config.setEndereco(enderecoField.getText());
        config.setMunicipio(municipioField.getText());
        config.setUf(ufField.getText());
        config.setCep(cepField.getText());
        config.setTelefone(telefoneField.getText());
        config.setEmail(emailField.getText());
        config.setCodigoIbge(codigoIbgeField.getText());
        config.setInscricaoEstadual(ieField.getText());
        config.setRegimeTributario(regimeField.getText());
        config.setCertificadoPath(certificadoPathField.getText());
        config.setSenhaCertificado(senhaCertificadoField.getText());
        config.setXmlPath(xmlPathField.getText());

        companyConfigRepository.save(config);
        showAlert("Sucesso", "Empresa salva com sucesso!", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void abrirListaDeEmpresas() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/EmpresaListView.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Empresas Cadastradas");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erro", "Erro ao abrir a lista de empresas.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void buscarEnderecoPorCep() {
        String cep = cepField.getText().replaceAll("[^\\d]", "");

        if (cep.length() != 8) {
            showAlert("Erro", "CEP inválido. Deve conter 8 dígitos.", Alert.AlertType.ERROR);
            return;
        }

        try {
            URL url = new URL("https://viacep.com.br/ws/" + cep + "/json/");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JSONObject json = new JSONObject(response.toString());

            if (json.has("erro")) {
                showAlert("Erro", "CEP não encontrado!", Alert.AlertType.ERROR);
                return;
            }

            enderecoField.setText(json.getString("logradouro"));
            municipioField.setText(json.getString("localidade"));
            ufField.setText(json.getString("uf"));
            codigoIbgeField.setText(json.getString("ibge"));

        } catch (IOException e) {
            showAlert("Erro", "Erro na conexão: " + e.getMessage(), Alert.AlertType.ERROR);
        } catch (Exception e) {
            showAlert("Erro", "Erro ao processar resposta do ViaCEP: " + e.getMessage(), Alert.AlertType.ERROR);
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
