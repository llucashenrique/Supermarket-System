package com.lctech.supermercado.service;

import com.lctech.supermercado.expection.ResourceNotFoundException;
import com.lctech.supermercado.model.Product;
import com.lctech.supermercado.repository.ProductsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    @Autowired
    private ProductsRepository productsRepository;

    // Criar um produto
    public Product createProduct(Product product) {
        log.info("Creating a new product: {}", product.getName());
        Product newProduct = productsRepository.save(product);
        log.info("Product created with ID: {}", newProduct.getId());
        return newProduct;
    }

    // Listar todos os produtos
    public List<Product> getAllProducts() {
        log.info("Fetching all products");
        List<Product> products = productsRepository.findAll();
        if (products.isEmpty()) {
            log.info("No products found");
        }
        return products;
    }

    // Buscar um produto pelo ID
    public Product getProductById(Long id) {
        log.info("Fetching product with ID: {}", id);
        return productsRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Product with ID {} not found", id);
                    return new ResourceNotFoundException("Product", "ID", id);
                });
    }

    // Atualizar um produto pelo ID
    public Product updateProduct(Long id, Product updatedProduct) {
        log.info("Updating product with ID: {}", id);
        Product existingProduct = productsRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Product with ID {} not found", id);
                    return new ResourceNotFoundException("Product", "ID", id);
                });

        existingProduct.setName(updatedProduct.getName());
        existingProduct.setBarcode(updatedProduct.getBarcode());
        existingProduct.setPrice(updatedProduct.getPrice());
        existingProduct.setStock(updatedProduct.getStock());

        Product savedProduct = productsRepository.save(existingProduct);
        log.info("Product updated with ID: {}", savedProduct.getId());
        return savedProduct;
    }

    // Atualizar apenas o estoque do produto
    public Product updateStock(Long id, Double stockDelta) {
        log.info("Updating stock for product with ID: {}", id);
        Product existingProduct = productsRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Product with ID {} not found", id);
                    return new ResourceNotFoundException("Product", "ID", id);
                });

        double updatedStock = existingProduct.getStock() + stockDelta;

        if (updatedStock < 0) {
            log.error("Stock cannot be negative for product ID: {}", id);
            throw new IllegalArgumentException("Stock cannot be negative");
        }

        existingProduct.setStock(updatedStock);
        Product savedProduct = productsRepository.save(existingProduct);
        log.info("Stock updated for product ID: {}, new stock: {}", id, updatedStock);
        return savedProduct;
    }

    // Adicionar estoque manualmente
    public Product addStockManually(Long id, Double quantityToAdd) {
        log.info("Adding stock manually for product ID: {}", id);
        if (quantityToAdd <= 0) {
            log.error("Invalid quantity to add: {}", quantityToAdd);
            throw new IllegalArgumentException("Quantity to add must be greater than zero");
        }
        return addStock(id, quantityToAdd);
    }

    // Adicionar estoque
    public Product addStock(Long id, Double quantity) {
        return updateStock(id, quantity);
    }

    // Subtrair estoque
    public Product subtractStock(Long id, Double quantity) {
        log.info("Subtracting stock for product ID: {}", id);
        if (quantity <= 0) {
            log.error("Invalid quantity to subtract: {}", quantity);
            throw new IllegalArgumentException("Quantity to subtract must be greater than zero");
        }
        return updateStock(id, -quantity);
    }

    // Deletar um produto pelo ID
    public void deleteProduct(Long id) {
        log.info("Deleting product with ID: {}", id);
        if (!productsRepository.existsById(id)) {
            log.error("Product with ID {} not found", id);
            throw new ResourceNotFoundException("Product", "ID", id);
        }
        productsRepository.deleteById(id);
        log.info("Product with ID: {} has been deleted", id);
    }

    // Pesquisar produtos com filtros avançados
    public List<Product> searchProducts(String name, Double minPrice, Double maxPrice, Double minStock, Double maxStock) {
        log.info("Searching products with filters");
        List<Product> products = productsRepository.searchProducts(name, minPrice, maxPrice, minStock, maxStock);
        if (products.isEmpty()) {
            log.info("No products match the search criteria");
        }
        return products;
    }
}
