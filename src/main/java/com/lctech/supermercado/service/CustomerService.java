package com.lctech.supermercado.service;

import com.lctech.supermercado.expection.ResourceNotFoundException;
import com.lctech.supermercado.model.Customers;
import com.lctech.supermercado.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerService.class);

    @Autowired
    private CustomerRepository customerRepository;

    // Create Customer
    public Customers createCustomer(Customers customer) {
        log.info("Creating a new customer");
        if (customer.getName() == null || customer.getName().isEmpty()) {
            log.error("Customer name cannot be null or empty");
            throw new IllegalArgumentException("Customer name cannot be null or empty");
        }
        Customers newCustomer = customerRepository.save(customer);
        log.info("Customer created with ID: {}", newCustomer.getId());
        return newCustomer;
    }

    // Get All Customers
    public List<Customers> getAllCustomers() {
        log.info("Fetching all customers");
        List<Customers> customers = customerRepository.findAll();
        if (customers.isEmpty()) {
            log.info("No customers found");
        }
        return customers;
    }

    // Get Customer by ID
    public Customers getCustomerById(Long customerId) {
        log.info("Fetching customer with ID: {}", customerId);
        return customerRepository.findById(customerId)
                .orElseThrow(() -> {
                    log.error("Customer with ID {} not found", customerId);
                    return new ResourceNotFoundException("Customer", "ID", customerId);
                });
    }

    // Get Customer by Document (CPF or CNPJ)
    public Customers getCustomerByDocument(String document) {
        log.info("Fetching customer by document: {}", document);

        Optional<Customers> optionalCustomer = customerRepository.findByCpfOrCnpj(
                document.matches("\\d{11}") ? document : null, // CPF (11 digits)
                document.matches("\\d{14}") ? document : null  // CNPJ (14 digits)
        );

        return optionalCustomer.orElseThrow(() -> {
            log.error("Customer with document {} not found", document);
            return new ResourceNotFoundException("Customer", "document", document);
        });
    }


    // Advanced Search with Pagination
    public Page<Customers> advancedSearch(String name, String email, String phone, String cpf, String cnpj, Pageable pageable) {
        log.info("Performing advanced search for customers");
        Page<Customers> customers = customerRepository.advancedSearch(name, email, phone, cpf, cnpj, pageable);
        if (customers.isEmpty()) {
            log.info("No customers match the search criteria");
        }
        return customers;
    }

    // Update Customer
    public Customers updateCustomer(Long id, Customers updatedCustomer) {
        log.info("Updating customer with ID: {}", id);
        Customers existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Customer with ID {} not found", id);
                    return new ResourceNotFoundException("Customer", "ID", id);
                });

        existingCustomer.setName(updatedCustomer.getName());
        existingCustomer.setEmail(updatedCustomer.getEmail());
        existingCustomer.setPhone(updatedCustomer.getPhone());
        existingCustomer.setAddress(updatedCustomer.getAddress());
        existingCustomer.setCpf(updatedCustomer.getCpf());
        existingCustomer.setCnpj(updatedCustomer.getCnpj());

        Customers savedCustomer = customerRepository.save(existingCustomer);
        log.info("Customer updated with ID: {}", savedCustomer.getId());
        return savedCustomer;
    }

    // Delete Customer
    public void deleteCustomer(Long id) {
        log.info("Deleting customer with ID: {}", id);
        Customers existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Customer with ID {} not found", id);
                    return new ResourceNotFoundException("Customer", "ID", id);
                });
        customerRepository.delete(existingCustomer);
        log.info("Customer with ID: {} has been deleted", id);
    }
}
