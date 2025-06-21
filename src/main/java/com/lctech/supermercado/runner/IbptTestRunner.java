package com.lctech.supermercado.runner;

import com.lctech.supermercado.dto.IbptResponse;
import com.lctech.supermercado.service.FiscalApiService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class IbptTestRunner implements CommandLineRunner {

    private final FiscalApiService api;

    public IbptTestRunner(FiscalApiService api) {
        this.api = api;
    }

    @Override
    public void run(String... args) throws Exception {
        String exemploNcm = "07141010";
        try {
            IbptResponse resp = api.lookupByNcm(exemploNcm);
            if (resp == null) {
                System.out.println("NCM não encontrado na tabela local.");
                return;
            }

            System.out.println("NCM: " + resp.getNcm());
            System.out.println("ICMS  : " + resp.getSituacao_tributaria().getIcms().getAliquota() + "%");
            System.out.println("PIS   : " + resp.getSituacao_tributaria().getPis().getAliquota() + "%");
            System.out.println("COFINS: " + resp.getSituacao_tributaria().getCofins().getAliquota() + "%");

            // Verifica se veio da API (por exemplo, se a resposta tiver algum campo extra que só a API retorna)
            boolean veioDaApi = resp.getSituacao_tributaria().getPis().getAliquota().compareTo(BigDecimal.ZERO) != 0;
            System.out.println("Fonte: " + (veioDaApi ? "API IBPT" : "Tabela Local"));

        } catch (Exception e) {
            System.err.println("Erro ao consultar NCM: " + e.getMessage());
        }
    }
}


