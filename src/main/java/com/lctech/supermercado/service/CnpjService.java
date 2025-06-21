package com.lctech.supermercado.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lctech.supermercado.model.Customers;
import org.springframework.stereotype.Service;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.util.Optional;

@Service
public class CnpjService {

    private static final String API_URL = "https://receitaws.com.br/v1/cnpj/";

    public Optional<Customers> fetchCustomerByCnpj(String cnpj) {
        try {
            // Validação do CNPJ
            if (!cnpj.matches("\\d{14}")) {
                throw new IllegalArgumentException("CNPJ inválido. Deve conter 14 dígitos.");
            }

            // Cria o cliente HTTP
            HttpClient client = HttpClient.newHttpClient();

            // Cria a requisição HTTP
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(API_URL + cnpj))
                    .header("Accept", "application/json")
                    .build();

            // Envia a requisição e captura a resposta
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Erro ao buscar dados do CNPJ. Código: " + response.statusCode());
            }

            // Converte a resposta JSON em um objeto Java
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(response.body());

            if (jsonNode.has("status") && jsonNode.get("status").asText().equalsIgnoreCase("ERROR")) {
                throw new RuntimeException("Erro na API: " + jsonNode.get("message").asText());
            }

            // Extrai os dados para criar um objeto Customers
            Customers customer = Customers.builder()
                    .name(jsonNode.has("nome") ? jsonNode.get("nome").asText() : null)
                    .address(jsonNode.has("logradouro") && jsonNode.has("numero")
                            ? jsonNode.get("logradouro").asText() + ", " + jsonNode.get("numero").asText()
                            : null)
                    .phone(jsonNode.has("telefone") ? jsonNode.get("telefone").asText() : null)
                    .email(jsonNode.has("email") ? jsonNode.get("email").asText() : null)
                    .cnpj(cnpj)
                    .build();

            return Optional.of(customer);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao consultar CNPJ: " + e.getMessage(), e);
        }
    }
}

