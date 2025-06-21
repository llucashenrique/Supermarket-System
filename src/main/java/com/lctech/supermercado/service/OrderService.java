package com.lctech.supermercado.service;

import com.lctech.supermercado.dto.TopProductDTO;
import com.lctech.supermercado.model.Customers;
import com.lctech.supermercado.model.Order;
import com.lctech.supermercado.model.Product;
import com.lctech.supermercado.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductService productService;


    public Long getOrderCountByDate(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        return orderRepository.countOrdersByDate(start, end);
    }

    public BigDecimal getTotalSalesByDate(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        BigDecimal total = orderRepository.sumOrdersByDate(start, end);
        return total != null ? total : BigDecimal.ZERO;
    }

    public Map<String, Double> getTotalSalesByPaymentTypeInMonth(int month) {
        List<Object[]> results = orderRepository.sumTotalByPaymentTypeInMonth(month);
        Map<String, Double> map = new HashMap<>();
        for (Object[] row : results) {
            String type = (String) row[0];
            Double total = ((Number) row[1]).doubleValue();
            map.put(type, total);
        }
        return map;
    }

    public Order createOrder(Customers customer, Product product, Double quantity) {
        log.info("Creating order for customer ID: {} and product ID: {}", customer.getId(), product.getId());
        if (quantity <= 0) {
            log.error("Invalid quantity: {}", quantity);
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        if (product.getStock() < quantity) {
            log.error("Not enough stock for product ID: {}", product.getId());
            throw new IllegalArgumentException("Not enough stock for this product");
        }

        productService.subtractStock(product.getId(), quantity);

        Order order = new Order();
        order.setCustomer(customer);
        order.addProduct(product);
        order.setQuantity(quantity);
        order.setTotalPrice(product.getPrice().doubleValue() * quantity);

        Order savedOrder = orderRepository.save(order);
        log.info("Order created successfully with ID: {}", savedOrder.getId());
        return savedOrder;
    }

    // Total vendido entre dois períodos
    public double getTotalSalesByDateRange(LocalDateTime start, LocalDateTime end) {
        BigDecimal total = orderRepository.sumOrdersByDate(start, end);
        return total != null ? total.doubleValue() : 0.0;
    }

    public double getMonthlyTotalByMonthAndYear(int month, int year) {
        Double result = orderRepository.getMonthlyTotalByMonthAndYear(month, year);
        return result != null ? result : 0.0;
    }
    // Quantidade de pedidos entre dois períodos
    public Long countOrdersByDateRange(LocalDateTime start, LocalDateTime end) {
        return orderRepository.countOrdersByDateRange(start, end);
    }

    // Total por tipo de pagamento entre dois períodos
    public double getTotalByPaymentTypeAndDate(String paymentType, LocalDateTime start, LocalDateTime end) {
        BigDecimal total = orderRepository.sumByPaymentTypeAndDate(paymentType, start, end);
        return total != null ? total.doubleValue() : 0.0;
    }

    // Total mensal (somatório de vendas de um determinado mês)
    public double getMonthlyTotalByMonth(int month) {
        BigDecimal total = orderRepository.sumMonthlyTotalByMonth(month);
        return total != null ? total.doubleValue() : 0.0;
    }

    public List<Order> findOrdersByDateRange(LocalDateTime start, LocalDateTime end) {
        return orderRepository.findByOrderDateBetween(start, end);
    }



    public List<Order> getAllOrders() {
        log.info("Fetching all orders");
        List<Order> orders = orderRepository.findAll();
        if (orders.isEmpty()) {
            log.info("No orders found");
        }
        return orders;
    }

    public List<Order> getOrdersByCustomer(Long customerId) {
        log.info("Fetching orders for customer ID: {}", customerId);
        List<Order> orders = orderRepository.findByCustomerId(customerId);
        if (orders.isEmpty()) {
            log.info("No orders found for customer ID: {}", customerId);
        }
        return orders;
    }

    public double getTotalByCustomer(Long customerId) {
        log.info("Calculating total for customer ID: {}", customerId);
        double total = orderRepository.calculateTotalByCustomerId(customerId)
                .orElse(0.0);
        log.info("Total for customer ID {}: {}", customerId, total);
        return total;
    }

    public List<TopProductDTO> getTopProducts() {
        log.info("Fetching top selling products");
        List<TopProductDTO> topProducts = orderRepository.findTopProducts().stream()
                .map(result -> new TopProductDTO(
                        ((Number) result[0]).longValue(),
                        (String) result[1],
                        ((Number) result[2]).longValue()))
                .toList();
        log.info("Top selling products fetched successfully");
        return topProducts;
    }

    public Long countTodayOrders() {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();
        return orderRepository.countOrdersByDate(start, end);
    }
}
