package com.lctech.supermercado.dto;

import java.math.BigDecimal;

public class IbptResponse {
    private String ncm;
    private SituacaoTributaria situacao_tributaria;


    public String getNcm() {
        return ncm;
    }

    public void setNcm(String ncm) {
        this.ncm = ncm;
    }

    public SituacaoTributaria getSituacao_tributaria() {
        return situacao_tributaria;
    }

    public void setSituacao_tributaria(SituacaoTributaria situacao_tributaria) {
        this.situacao_tributaria = situacao_tributaria;
    }

    public static class SituacaoTributaria {
        private Aliquota icms;
        private Aliquota pis;
        private Aliquota cofins;

        public Aliquota getIcms() {
            return icms;
        }

        public void setIcms(Aliquota icms) {
            this.icms = icms;
        }

        public Aliquota getPis() {
            return pis;
        }

        public void setPis(Aliquota pis) {
            this.pis = pis;
        }

        public Aliquota getCofins() {
            return cofins;
        }

        public void setCofins(Aliquota cofins) {
            this.cofins = cofins;
        }
    }

    public static class Aliquota {
        private BigDecimal aliquota;


        public BigDecimal getAliquota() {
            return aliquota;
        }

        public void setAliquota(BigDecimal aliquota) {
            this.aliquota = aliquota;
        }
    }
}

