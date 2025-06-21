package com.lctech.supermercado.controller;

import com.lctech.supermercado.model.Customers;
import com.lctech.supermercado.model.Order;
import com.lctech.supermercado.model.Product;
import com.lctech.supermercado.service.CustomerService;
import com.lctech.supermercado.service.OrderService;
import com.lctech.supermercado.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ProductService productService;

    // Criar um pedido
    @PostMapping
    public ResponseEntity<Order> createOrder(
            @RequestParam Long customerId,
            @RequestParam Long productId,
            @RequestParam Double quantity) {
        log.info("Creating order for customer ID: {} and product ID: {}", customerId, productId);

        try {
            Customers customer = customerService.getCustomerById(customerId);
            Product product = productService.getProductById(productId);

            if (quantity <= 0) {
                log.error("Invalid quantity: {}", quantity);
                return ResponseEntity.badRequest().body(null);
            }

            Order order = orderService.createOrder(customer, product, quantity);
            log.info("Order created successfully with ID: {}", order.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(order);
        } catch (Exception e) {
            log.error("Failed to create order: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // Listar todos os pedidos
    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        log.info("Fetching all orders");
        List<Order> orders = orderService.getAllOrders();
        if (orders.isEmpty()) {
            log.info("No orders found");
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(orders);
    }

    // Buscar pedidos por cliente
    @GetMapping("/by-customer")
    public ResponseEntity<List<Order>> getOrdersByCustomer(@RequestParam Long customerId) {
        log.info("Fetching orders for customer ID: {}", customerId);
        try {
            List<Order> orders = orderService.getOrdersByCustomer(customerId);
            if (orders.isEmpty()) {
                log.info("No orders found for customer ID: {}", customerId);
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            log.error("Failed to fetch orders: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }


    // Calcular o total de pedidos por cliente
    @GetMapping("/total-by-customer")
    public ResponseEntity<Double> getTotalByCustomer(@RequestParam Long customerId) {
        log.info("Calculating total for customer ID: {}", customerId);
        try {
            double total = orderService.getTotalByCustomer(customerId);
            return ResponseEntity.ok(total);
        } catch (Exception e) {
            log.error("Failed to calculate total: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Obter produtos mais vendidos
    @GetMapping("/top-products")
    public ResponseEntity<List<?>> getTopProducts() {
        log.info("Fetching top selling products");
        try {
            List<?> topProducts = orderService.getTopProducts();
            return ResponseEntity.ok(topProducts);
        } catch (Exception e) {
            log.error("Failed to fetch top products: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}