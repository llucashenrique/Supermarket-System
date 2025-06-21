package com.lctech.supermercado.service;

import org.apache.xml.security.algorithms.MessageDigestAlgorithm;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.transforms.Transforms;
import org.apache.xml.security.utils.Constants;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.w3c.dom.*;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.*;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.URL;
import java.security.*;
import java.security.cert.X509Certificate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import com.lctech.supermercado.model.CompanyConfig;
import org.w3c.dom.Node;

public class NfeService {

    private PrivateKey privateKey;
    private X509Certificate certificate;

    static {
        org.apache.xml.security.Init.init();
        Security.addProvider(new BouncyCastleProvider());
    }

    public void carregarCertificado(String caminhoPfx, String senha) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(new FileInputStream(caminhoPfx), senha.toCharArray());
        String alias = keyStore.aliases().nextElement();
        this.privateKey = (PrivateKey) keyStore.getKey(alias, senha.toCharArray());
        this.certificate = (X509Certificate) keyStore.getCertificate(alias);
        System.out.println("✅ Certificado carregado com sucesso!");
    }

    public Document gerarXmlNFCe(CompanyConfig empresa) throws Exception {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setNamespaceAware(true);
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();

        Element nfe = doc.createElementNS("http://www.portalfiscal.inf.br/nfe", "NFe");
        doc.appendChild(nfe);

        String chave = gerarChaveFake();
        String idNota = "NFe" + chave;

        Element infNFe = doc.createElement("infNFe");
        infNFe.setAttribute("Id", idNota);
        infNFe.setAttribute("versao", "4.00");
        infNFe.setIdAttribute("Id", true);

        Element ide = doc.createElement("ide");
        ide.appendChild(createElement(doc, "cUF", "35"));
        ide.appendChild(createElement(doc, "natOp", "VENDA CONSUMIDOR"));
        ide.appendChild(createElement(doc, "mod", "65"));
        ide.appendChild(createElement(doc, "serie", "1"));
        ide.appendChild(createElement(doc, "nNF", "1"));
        ide.appendChild(createElement(doc, "dhEmi", OffsetDateTime.now(ZoneId.of("America/Sao_Paulo")).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)));
        ide.appendChild(createElement(doc, "tpNF", "1"));
        ide.appendChild(createElement(doc, "idDest", "1"));
        ide.appendChild(createElement(doc, "cMunFG", "3550308"));
        ide.appendChild(createElement(doc, "tpImp", "4"));
        ide.appendChild(createElement(doc, "tpEmis", "1"));
        ide.appendChild(createElement(doc, "cDV", "0"));
        ide.appendChild(createElement(doc, "tpAmb", Boolean.TRUE.equals(empresa.getProducao()) ? "1" : "2"));
        ide.appendChild(createElement(doc, "finNFe", "1"));
        ide.appendChild(createElement(doc, "indFinal", "1"));
        ide.appendChild(createElement(doc, "indPres", "1"));
        ide.appendChild(createElement(doc, "procEmi", "0"));
        ide.appendChild(createElement(doc, "verProc", "1.0"));

        Element emit = doc.createElement("emit");
        emit.appendChild(createElement(doc, "CNPJ", empresa.getCnpj().replaceAll("[^0-9]", "")));
        emit.appendChild(createElement(doc, "xNome", empresa.getRazaoSocial()));

        Element det = doc.createElement("det");
        det.setAttribute("nItem", "1");
        Element prod = doc.createElement("prod");
        prod.appendChild(createElement(doc, "cProd", "001"));
        prod.appendChild(createElement(doc, "xProd", "Produto Teste"));
        prod.appendChild(createElement(doc, "qCom", "1.0000"));
        prod.appendChild(createElement(doc, "vUnCom", "10.00"));
        prod.appendChild(createElement(doc, "vProd", "10.00"));
        det.appendChild(prod);

        Element imposto = doc.createElement("imposto");
        Element icms = doc.createElement("ICMS");
        Element icms00 = doc.createElement("ICMS00");
        icms00.appendChild(createElement(doc, "orig", "0"));
        icms00.appendChild(createElement(doc, "CST", "00"));
        icms00.appendChild(createElement(doc, "modBC", "3"));
        icms00.appendChild(createElement(doc, "vBC", "10.00"));
        icms00.appendChild(createElement(doc, "pICMS", "0.00"));
        icms00.appendChild(createElement(doc, "vICMS", "0.00"));
        icms.appendChild(icms00);
        imposto.appendChild(icms);
        det.appendChild(imposto);

        Element total = doc.createElement("total");
        Element ICMSTot = doc.createElement("ICMSTot");
        ICMSTot.appendChild(createElement(doc, "vProd", "10.00"));
        ICMSTot.appendChild(createElement(doc, "vNF", "10.00"));
        total.appendChild(ICMSTot);

        infNFe.appendChild(ide);
        infNFe.appendChild(emit);
        infNFe.appendChild(det);
        infNFe.appendChild(total);

        nfe.appendChild(infNFe);
        return doc;
    }

    private Element createElement(Document doc, String name, String value) {
        Element el = doc.createElement(name);
        el.setTextContent(value);
        return el;
    }

    private String gerarChaveFake() {
        StringBuilder chave = new StringBuilder();
        while (chave.length() < 44) {
            chave.append(UUID.randomUUID().toString().replaceAll("[^A-Za-z0-9]", ""));
        }
        return chave.substring(0, 44);
    }

    public Document assinarXml(Document xmlDoc, String tagAssinatura, String idElement) throws Exception {
        XMLSignature signature = new XMLSignature(xmlDoc, "", XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256);
        xmlDoc.getElementsByTagName(tagAssinatura).item(0).appendChild(signature.getElement());

        Transforms transforms = new Transforms(xmlDoc);
        transforms.addTransform(Transforms.TRANSFORM_ENVELOPED_SIGNATURE);
        transforms.addTransform(Transforms.TRANSFORM_C14N_EXCL_OMIT_COMMENTS);

        signature.addDocument("#" + idElement, transforms, MessageDigestAlgorithm.ALGO_ID_DIGEST_SHA256);
        signature.addKeyInfo(certificate);
        signature.addKeyInfo(certificate.getPublicKey());
        signature.sign(privateKey);

        // ✅ Salvar XML assinado
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        FileWriter fw = new FileWriter("nfe_gerada.xml"); // ou use o nome baseado na chave
        transformer.transform(new DOMSource(xmlDoc), new StreamResult(fw));
        System.out.println("✅ XML assinado salvo com sucesso!");

        return xmlDoc;
    }


    public void enviarParaSefaz(Document xmlAssinado, CompanyConfig empresa) throws Exception {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        transformer.transform(new DOMSource(xmlAssinado), new StreamResult(output));
        String xmlString = output.toString("UTF-8").replaceFirst("<\\?xml.*?\\?>", "").trim();

        StringBuilder soapXml = new StringBuilder();
        soapXml.append("<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\">")
                .append("<soap:Header>")
                .append("<nfeCabecMsg xmlns=\"http://www.portalfiscal.inf.br/nfe/wsdl/NFeAutorizacao4\">")
                .append("<cUF>35</cUF>")
                .append("<versaoDados>4.00</versaoDados>")
                .append("</nfeCabecMsg>")
                .append("</soap:Header>")
                .append("<soap:Body>")
                .append("<nfeDadosMsg xmlns=\"http://www.portalfiscal.inf.br/nfe/wsdl/NFeAutorizacao4\">")
                .append(xmlString)
                .append("</nfeDadosMsg>")
                .append("</soap:Body>")
                .append("</soap:Envelope>");

        // ⚠️ Criar conexão HTTPS com autenticação mútua
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        if (empresa.getCertificadoPath() == null || empresa.getSenhaCertificado() == null) {
            throw new IllegalArgumentException("Certificado ou senha não informados.");
        }
        keyStore.load(new FileInputStream(empresa.getCertificadoPath()), empresa.getSenhaCertificado().toCharArray());

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(keyStore, empresa.getSenhaCertificado().toCharArray());

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init((KeyStore) null); // usar os certificados confiáveis do Java (cacerts)

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());

        URL url = new URL(Boolean.TRUE.equals(empresa.getProducao())
                ? "https://nfce.fazenda.sp.gov.br/ws/NFeAutorizacao4.asmx"
                : "https://homologacao.nfce.fazenda.sp.gov.br/ws/NFeAutorizacao4.asmx");


        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/soap+xml; charset=utf-8");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(soapXml.toString().getBytes("UTF-8"));
        }

        InputStream responseStream = conn.getInputStream();
        String response = new BufferedReader(new InputStreamReader(responseStream))
                .lines().reduce("", (acc, line) -> acc + line + "\n");

        System.out.println("📥 Resposta da SEFAZ:\n" + response);
    }
}