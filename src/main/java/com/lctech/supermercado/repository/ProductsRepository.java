package com.lctech.supermercado.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.lctech.supermercado.model.Product;

public interface ProductsRepository extends JpaRepository<Product, Long> {
    // Adicione métodos personalizados, se necessário
}

