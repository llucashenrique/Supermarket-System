package com.lctech.supermercado.model;

import jakarta.persistence.*;

@Entity
@Table(name = "company_config")
public class CompanyConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nomeFantasia;
    private String razaoSocial;
    private String cnpj;
    private String inscricaoEstadual;
    private String endereco;
    private String municipio;
    private String uf;
    private String cep;
    private String codigoIbge;
    private String telefone;
    private String email;
    private String regimeTributario;
    private String certificadoPath;
    private String senhaCertificado;
    private String xmlPath;
    @Column(name = "ambiente_producao")
    private Boolean producao;
    private String caminhoCertificado;

    @Column(length = 60)
    private String csc;

    @Column(length = 5)
    private String idToken;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNomeFantasia() { return nomeFantasia; }
    public void setNomeFantasia(String nomeFantasia) { this.nomeFantasia = nomeFantasia; }

    public String getRazaoSocial() { return razaoSocial; }
    public void setRazaoSocial(String razaoSocial) { this.razaoSocial = razaoSocial; }

    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }

    public String getInscricaoEstadual() { return inscricaoEstadual; }
    public void setInscricaoEstadual(String inscricaoEstadual) { this.inscricaoEstadual = inscricaoEstadual; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    public String getMunicipio() { return municipio; }
    public void setMunicipio(String municipio) { this.municipio = municipio; }

    public String getUf() { return uf; }
    public void setUf(String uf) { this.uf = uf; }

    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }

    public String getCodigoIbge() { return codigoIbge; }
    public void setCodigoIbge(String codigoIbge) { this.codigoIbge = codigoIbge; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRegimeTributario() { return regimeTributario; }
    public void setRegimeTributario(String regimeTributario) { this.regimeTributario = regimeTributario; }

    public String getCertificadoPath() { return certificadoPath; }
    public void setCertificadoPath(String certificadoPath) { this.certificadoPath = certificadoPath; }

    public String getSenhaCertificado() { return senhaCertificado; }
    public void setSenhaCertificado(String senhaCertificado) { this.senhaCertificado = senhaCertificado; }

    public String getXmlPath() { return xmlPath; }
    public void setXmlPath(String xmlPath) { this.xmlPath = xmlPath; }

    public Boolean getProducao() {
        return producao != null ? producao : false;
    }

    public void setProducao(Boolean producao) {
        this.producao = producao;
    }


    public String getCaminhoCertificado() { return caminhoCertificado; }
    public void setCaminhoCertificado(String caminhoCertificado) { this.caminhoCertificado = caminhoCertificado; }

    public String getCsc() { return csc; }
    public void setCsc(String csc) { this.csc = csc; }

    public String getIdToken() { return idToken; }
    public void setIdToken(String idToken) { this.idToken = idToken; }
}
