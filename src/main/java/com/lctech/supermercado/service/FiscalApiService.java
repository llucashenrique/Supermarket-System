package com.lctech.supermercado.service;

import com.lctech.supermercado.dto.IbptResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
public class FiscalApiService {

    @Value("${ibpt.token}")
    private String token;

    private final RestTemplate restTemplate;
    private final Map<String, IbptResponse> cacheLocal = new HashMap<>();

    public FiscalApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        carregarTabelaLocal();
    }

    public IbptResponse lookupByNcm(String ncm) {
        try {
            String url = String.format("https://api.ibpt.org.br/api/v1/ncm/%s?token=%s", ncm, token);
            return restTemplate.getForObject(url, IbptResponse.class);
        } catch (Exception e) {
            System.err.println("Erro na API IBPT. Consultando tabela local: " + e.getMessage());
            try {
                String codigoNormalizado = String.format("%08d", Integer.parseInt(ncm));
                return cacheLocal.get(codigoNormalizado);
            } catch (NumberFormatException ex) {
                return null;
            }
        }
    }

    private void carregarTabelaLocal() {
        try {
            ClassPathResource resource = new ClassPathResource("TabelaIBPTaxSP25.1.E.csv");
            BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.ISO_8859_1));
            String linha;
            reader.readLine(); // pula cabeçalho
            while ((linha = reader.readLine()) != null) {
                String[] partes = linha.split(";");
                if (partes.length >= 7) {
                    String raw = partes[0].trim();
                    String codigo = String.format("%08d", Integer.parseInt(raw)); // força 8 dígitos com zero à esquerda
                    String descricao = partes[3].trim();
                    BigDecimal aliqNacional = new BigDecimal(partes[4].trim().replace(",", "."));
                    BigDecimal aliqEstadual = new BigDecimal(partes[6].trim().replace(",", "."));

                    IbptResponse.Aliquota icms = new IbptResponse.Aliquota();
                    icms.setAliquota(aliqEstadual);
                    IbptResponse.Aliquota pis = new IbptResponse.Aliquota();
                    pis.setAliquota(BigDecimal.ZERO); // Não há detalhamento no CSV
                    IbptResponse.Aliquota cofins = new IbptResponse.Aliquota();
                    cofins.setAliquota(BigDecimal.ZERO);

                    IbptResponse.SituacaoTributaria st = new IbptResponse.SituacaoTributaria();
                    st.setIcms(icms);
                    st.setPis(pis);
                    st.setCofins(cofins);

                    IbptResponse response = new IbptResponse();
                    response.setNcm(codigo);
                    response.setSituacao_tributaria(st);

                    cacheLocal.put(codigo, response);
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar a tabela local IBPT: " + e.getMessage());
        }
    }
}
