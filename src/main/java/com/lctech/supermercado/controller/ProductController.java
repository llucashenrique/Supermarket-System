package com.lctech.supermercado.controller;

import com.lctech.supermercado.expection.ResourceNotFoundException;
import com.lctech.supermercado.model.Product;
import com.lctech.supermercado.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductService productService;

    // Listar todos os produtos
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        log.info("Fetching all products");
        List<Product> products = productService.getAllProducts();
        if (products.isEmpty()) {
            log.info("No products found");
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(products);
    }

    // Buscar produtos com filtros avançados
    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam(required = false) String name,
                                                        @RequestParam(required = false) Double minPrice,
                                                        @RequestParam(required = false) Double maxPrice,
                                                        @RequestParam(required = false) Double minStock,
                                                        @RequestParam(required = false) Double maxStock) {
        log.info("Searching products with filters");
        List<Product> products = productService.searchProducts(name, minPrice, maxPrice, minStock, maxStock);
        return ResponseEntity.ok(products);
    }

    // Buscar produto por ID
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        log.info("Fetching product with ID: {}", id);
        Product product = productService.getProductById(id);
        if (product == null) {
            log.error("Product with ID {} not found", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(product);
    }

    // Atualizar um produto pelo ID
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @Valid @RequestBody Product product) {
        log.info("Updating product with ID: {}", id);
        Product updatedProduct = productService.updateProduct(id, product);
        if (updatedProduct == null) {
            log.error("Failed to update product with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(updatedProduct);
    }

    // Atualizar apenas o estoque de um produto
    @PutMapping("/{id}/update-stock")
    public ResponseEntity<Product> updateProductStock(@PathVariable Long id, @RequestParam Double stockDelta) {
        log.info("Updating stock for product with ID: {}", id);
        if (stockDelta == null || stockDelta == 0) {
            log.error("Invalid stockDelta value: {}", stockDelta);
            return ResponseEntity.badRequest().build();
        }
        Product updatedProduct = productService.updateStock(id, stockDelta);
        return ResponseEntity.ok(updatedProduct);
    }

    // Adicionar estoque manualmente
    @PutMapping("/{id}/add-stock")
    public ResponseEntity<Product> addStockManually(@PathVariable Long id, @RequestParam Double quantityToAdd) {
        log.info("Adding stock manually for product with ID: {}", id);
        if (quantityToAdd == null || quantityToAdd <= 0) {
            log.error("Invalid quantityToAdd value: {}", quantityToAdd);
            return ResponseEntity.badRequest().build();
        }
        Product updatedProduct = productService.addStockManually(id, quantityToAdd);
        return ResponseEntity.ok(updatedProduct);
    }

    // Subtrair estoque manualmente
    @PutMapping("/{id}/subtract-stock")
    public ResponseEntity<Product> subtractStock(@PathVariable Long id, @RequestParam Double quantityToSubtract) {
        log.info("Subtracting stock manually for product with ID: {}", id);
        if (quantityToSubtract == null || quantityToSubtract <= 0) {
            log.error("Invalid quantityToSubtract value: {}", quantityToSubtract);
            return ResponseEntity.badRequest().build();
        }
        Product updatedProduct = productService.subtractStock(id, quantityToSubtract);
        return ResponseEntity.ok(updatedProduct);
    }

    // Deletar um produto pelo ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        log.info("Deleting product with ID: {}", id);
        try {
            productService.deleteProduct(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            log.error("Failed to delete product: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Endpoint de depuração (para testes)
    @PostMapping("/debug")
    public void debugEndpoint(@RequestBody String payload) {
        log.info("Received payload: {}", payload);
    }
}

