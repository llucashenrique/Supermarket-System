package com.lctech.supermercado.service;

import com.lctech.supermercado.ResourceNotFoundException;
import com.lctech.supermercado.model.Product;
import com.lctech.supermercado.repository.ProductsRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductsRepository productsRepository;

    // Criar um produto
    public Product createProduct(Product product) {
        return productsRepository.save(product);
    }

    // Listar todos os produtos
    public List<Product> getAllProducts() {
        return productsRepository.findAll();
    }

    // Atualizar um produto pelo ID
    public Product updateProduct(Long id, Product updatedProduct) {
        Product existingProduct = productsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product with ID " + id + " not found"));

        // Atualiza apenas os campos modificados
        existingProduct.setName(updatedProduct.getName());
        existingProduct.setBarcode(updatedProduct.getBarcode());
        existingProduct.setPrice(updatedProduct.getPrice());
        existingProduct.setStock(updatedProduct.getStock());

        return productsRepository.save(existingProduct);
    }

    public void deleteProduct(Long id) {
        if (!productsRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with ID: " + id);
        }
        productsRepository.deleteById(id);
    }


}


