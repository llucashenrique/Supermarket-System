package com.lctech.supermercado.controller;

import com.lctech.supermercado.model.Customers;
import com.lctech.supermercado.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/customers")
public class CustomerRestController {

    private static final Logger log = LoggerFactory.getLogger(CustomerController.class);

    @Autowired
    private CustomerService customerService;

    // Create Customer
    @PostMapping
    public ResponseEntity<Customers> createCustomer(@Valid @RequestBody Customers customer) {
        log.info("Creating new customer: {}", customer.getName());
        try {
            Customers newCustomer = customerService.createCustomer(customer);
            return ResponseEntity.status(HttpStatus.CREATED).body(newCustomer);
        } catch (IllegalArgumentException e) {
            log.error("Failed to create customer: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // Get All Customers
    @GetMapping
    public ResponseEntity<List<Customers>> getAllCustomers() {
        log.info("Fetching all customers");
        List<Customers> customers = customerService.getAllCustomers();
        if (customers.isEmpty()) {
            log.info("No customers found");
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(customers);
    }

    // Search Customers with Advanced Filters and Pagination
    @GetMapping("/search")
    public ResponseEntity<Page<Customers>> searchCustomers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String cpf,
            @RequestParam(required = false) String cnpj,
            Pageable pageable) {
        log.info("Searching customers with advanced filters");
        Page<Customers> customers = customerService.advancedSearch(name, email, phone, cpf, cnpj, pageable);
        if (customers.isEmpty()) {
            log.info("No customers match the search criteria");
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(customers);
    }

    // Get Customer by ID
    @GetMapping("/{id}")
    public ResponseEntity<Customers> getCustomerById(@PathVariable Long id) {
        log.info("Fetching customer with ID: {}", id);
        try {
            Customers customer = customerService.getCustomerById(id);
            return ResponseEntity.ok(customer);
        } catch (Exception e) {
            log.error("Customer not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Update Customer
    @PutMapping("/{id}")
    public ResponseEntity<Customers> updateCustomer(@PathVariable Long id, @Valid @RequestBody Customers customer) {
        log.info("Updating customer with ID: {}", id);
        try {
            Customers updatedCustomer = customerService.updateCustomer(id, customer);
            return ResponseEntity.ok(updatedCustomer);
        } catch (Exception e) {
            log.error("Failed to update customer: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Delete Customer
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        log.info("Deleting customer with ID: {}", id);
        try {
            customerService.deleteCustomer(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Failed to delete customer: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Get Customers by CPF or CNPJ
    @GetMapping("/document")
    public ResponseEntity<Customers> getCustomerByDocument(@RequestParam String document) {
        log.info("Fetching customer by document: {}", document);
        try {
            Customers customer = customerService.getCustomerByDocument(document);
            return ResponseEntity.ok(customer);
        } catch (Exception e) {
            log.error("Customer not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
