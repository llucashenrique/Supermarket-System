package com.lctech.supermercado.impressora;

import com.lctech.supermercado.model.CartItem;
import com.lctech.supermercado.model.CompanyConfig;
import com.lctech.supermercado.model.Order;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class CupomGenerator {

    public String gerarCupom(CompanyConfig empresa, Order venda, int numeroCupom) {
        StringBuilder sb = new StringBuilder();

        // Cabeçalho fiscal
        sb.append(center("******************************")).append("\n");
        sb.append(center(empresa.getRazaoSocial().toUpperCase())).append("\n");
        sb.append(center("CNPJ: " + formatarCnpj(empresa.getCnpj()))).append("\n");
        sb.append(center("IE: " + empresa.getInscricaoEstadual())).append("\n");
        sb.append(center(empresa.getEndereco())).append("\n");
        sb.append(center("******************************")).append("\n\n");

        // Info do cupom
        sb.append("CUPOM Nº: ").append(String.format("%06d", numeroCupom)).append("\n");
        sb.append("DATA: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))).append("\n");

        // CPF/CNPJ do consumidor
        if (venda.getDocumentoConsumidor() != null && !venda.getDocumentoConsumidor().isBlank()) {
            sb.append("CPF/CNPJ: ").append(formatarDocumento(venda.getDocumentoConsumidor())).append("\n");
        }

        sb.append("----------------------------------------\n");

        // Itens da venda
        sb.append(String.format("%-20s %5s %6s %7s\n", "ITEM", "QTD", "UN", "TOTAL"));

        for (CartItem item : venda.getItems()) {
            String nome = item.getProduct().getName();
            double qtd = item.getQuantity();
            double preco = item.getProduct().getPrice().doubleValue();
            double total = item.getTotalPrice().doubleValue();

            sb.append(String.format("%-20s %5.2f %6.2f %7.2f\n",
                    abreviar(nome, 20),
                    qtd,
                    preco,
                    total));
        }

        sb.append("\nTOTAL: R$ ").append(String.format("%.2f", venda.getTotalPrice())).append("\n");
        sb.append("Pagamento: ").append(venda.getPaymentType()).append("\n");

        sb.append("\n----------------------------------------\n");
        sb.append(center("OBRIGADO PELA PREFERÊNCIA!")).append("\n\n\n");

        return sb.toString();
    }

    public String gerarDanfeNfce(CompanyConfig empresa, Order order, String chaveAcesso, String urlConsulta) {
        StringBuilder sb = new StringBuilder();

        sb.append(empresa.getNomeFantasia().toUpperCase()).append("\n");
        sb.append("CNPJ: ").append(empresa.getCnpj()).append("\n\n");

        sb.append("DOCUMENTO AUXILIAR DA NFC-e\n");
        sb.append("------------------------------\n");

        sb.append("Data/Hora: ").append(order.getOrderDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n");
        sb.append("Nº Pedido: ").append(order.getId()).append("\n");
        sb.append("Forma de Pagamento: ").append(order.getPaymentType()).append("\n");
        sb.append(String.format(Locale.US, "Valor Total: R$ %.2f\n", order.getTotalPrice()));

        sb.append("\nProdutos:\n");
        for (CartItem item : order.getItems()) {
            String nome = item.getProduct().getName();
            BigDecimal preco = item.getTotalPrice().divide(BigDecimal.valueOf(item.getQuantity()), 2, RoundingMode.HALF_UP);
            sb.append(String.format("- %-18s %dx R$ %.2f\n", nome, (int) item.getQuantity(), preco.doubleValue()));
        }

        sb.append("------------------------------\n");
        sb.append("Chave de Acesso:\n");
        sb.append(chaveAcesso).append("\n\n");

        // Geração do texto do QR Code completo (padrão SEFAZ)
        String versaoQrCode = "2";
        String ambiente = Boolean.TRUE.equals(empresa.getProducao()) ? "1" : "2";
        String idToken = empresa.getIdToken();
        String csc = empresa.getCsc();

        String qrCodeText = (Boolean.TRUE.equals(empresa.getProducao())
                ? "https://www.nfce.fazenda.sp.gov.br/qrcode"
                : "https://www.homologacao.nfce.fazenda.sp.gov.br/qrcode")
                + "?p=" + chaveAcesso + "|" + versaoQrCode + "|" + ambiente + "|" + idToken + "|" + csc;

        sb.append("Consulta QR Code:\n");
        sb.append(qrCodeText).append("\n\n");

        sb.append("Este documento é válido como DANFE NFC-e.\n");
        if (!Boolean.TRUE.equals(empresa.getProducao())) {
            sb.append("Emitido em ambiente de homologação - sem valor fiscal\n");
        }

        return sb.toString();
    }

    private String center(String text) {
        int width = 40;
        int padSize = (width - text.length()) / 2;
        return " ".repeat(Math.max(padSize, 0)) + text;
    }

    private String abreviar(String texto, int max) {
        return texto.length() <= max ? texto : texto.substring(0, max - 1) + "…";
    }

    private String formatarCnpj(String cnpj) {
        return cnpj.replaceAll("(\\d{2})(\\d{3})(\\d{3})(\\d{4})(\\d{2})", "$1.$2.$3/$4-$5");
    }

    private String formatarCpf(String cpf) {
        return cpf.replaceAll("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4");
    }

    private String formatarDocumento(String doc) {
        if (doc.length() == 11) return formatarCpf(doc);
        if (doc.length() == 14) return formatarCnpj(doc);
        return doc; // já está formatado ou inválido
    }
}