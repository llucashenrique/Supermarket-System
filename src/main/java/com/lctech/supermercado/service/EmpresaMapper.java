package com.lctech.supermercado.service;

import com.lctech.supermercado.dto.EmpresaDTO;
import com.lctech.supermercado.model.Customers;

public class EmpresaMapper {

    public static EmpresaDTO fromCustomer(Customers customer) {
        EmpresaDTO dto = new EmpresaDTO();

        dto.setRazaoSocial(customer.getName());
        dto.setNomeFantasia(customer.getName()); // Substitua se tiver outro campo
        dto.setEndereco(customer.getAddress());

        // Campos extras, se disponíveis na sua API
        // dto.setInscricaoEstadual(customer.getIe());
        // dto.setRegimeTributario(customer.getRegime());

        return dto;
    }
}

