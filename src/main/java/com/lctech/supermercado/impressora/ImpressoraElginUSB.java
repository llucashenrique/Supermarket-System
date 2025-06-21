package com.lctech.supermercado.impressora;

import com.github.anastaciocintra.escpos.EscPos;
import com.github.anastaciocintra.escpos.EscPosConst;
import com.github.anastaciocintra.escpos.Style;
import com.github.anastaciocintra.escpos.barcode.QRCode;
import com.github.anastaciocintra.output.PrinterOutputStream;
import com.lctech.supermercado.model.CompanyConfig;
import com.lctech.supermercado.model.Order;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;

public class ImpressoraElginUSB implements ImpressoraService {

    private final CompanyConfig empresa;
    private final Order order;

    public ImpressoraElginUSB(CompanyConfig empresa, Order order) {
        this.empresa = empresa;
        this.order = order;
    }

    @Override
    public void imprimir(String conteudo) {
        try {
            PrintService printService = PrintServiceLookup.lookupDefaultPrintService();
            if (printService == null) {
                System.out.println("⚠️ Nenhuma impressora encontrada.");
                return;
            }

            EscPos escpos = new EscPos(new PrinterOutputStream(printService));

            Style titulo = new Style()
                    .setBold(true)
                    .setFontSize(Style.FontSize._2, Style.FontSize._2)
                    .setJustification(Style.Justification.Center);

            Style normalCenter = new Style().setJustification(Style.Justification.Center);
            Style normal = new Style();

            escpos.writeLF(titulo, empresa.getNomeFantasia() != null ? empresa.getNomeFantasia() : "");
            escpos.writeLF(normalCenter, "DOCUMENTO AUXILIAR DA NFC-e");
            escpos.writeLF("--------------------------------");


            for (String linha : conteudo.split("\n")) {
                if (linha != null) {
                    escpos.writeLF(normal, linha);
                }
            }

            escpos.writeLF("--------------------------------");

            // QR Code fiscal (padrão SEFAZ-SP)
            String chaveAcesso = order.getChave() != null ? order.getChave() : "";
            String ambiente = "2"; // 1 = produção, 2 = homologação
            String versaoQrCode = "2";
            String csc = empresa.getCsc() != null ? empresa.getCsc() : "";
            String idToken = empresa.getIdToken() != null ? empresa.getIdToken() : "";

            String urlConsulta = "https://www.homologacao.nfce.fazenda.sp.gov.br/qrcode";
            String qrCodeText = urlConsulta + "?p=" + chaveAcesso + "|" + versaoQrCode + "|" + ambiente + "|" + csc + "|" + idToken;

            escpos.writeLF(normalCenter, "Consulte pela chave abaixo:");
            escpos.writeLF(normalCenter, chaveAcesso);
            escpos.writeLF(normalCenter, "");

            QRCode qrcode = new QRCode();
            qrcode.setSize(6);
            qrcode.setJustification(EscPosConst.Justification.Center);

            escpos.write(qrcode, qrCodeText);

            escpos.feed(5);
            escpos.cut(EscPos.CutMode.FULL);
            escpos.close();

            System.out.println("✅ Cupom fiscal com QR Code enviado para impressora Elgin.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
