package com.lctech.supermercado.repository;

import com.lctech.supermercado.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;


public interface CustomerRepository extends JpaRepository<Customer, Long> {
    // Adicione métodos personalizados, se necessário
}

