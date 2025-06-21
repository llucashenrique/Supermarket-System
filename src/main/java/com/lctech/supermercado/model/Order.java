package com.lctech.supermercado.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import javafx.beans.property.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = true) // Cliente é opcional
    private Customers customer;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @NotNull(message = "Quantity is required.")
    private Double quantity;

    @NotNull(message = "Total price is required.")
    private Double totalPrice;

    @Column(nullable = false)
    private boolean pago = false;

    @Column(name = "payment_type")
    private String paymentType;

    @Column
    private LocalDateTime dataPagamento;

    @Column(length = 44)
    private String chave; // chave de acesso da NFC-e


    // Propriedades JavaFX para UI
    @Transient
    private final LongProperty idProperty = new SimpleLongProperty();

    @Transient
    private final StringProperty customerProperty = new SimpleStringProperty();

    @Transient
    private final ObjectProperty<BigDecimal> totalAmountProperty = new SimpleObjectProperty<>();

    @Transient
    private final StringProperty orderDateProperty = new SimpleStringProperty();

    private String documentoConsumidor;

    // Construtor padrão JPA
    public Order() {}

    public Order(Customers customer, List<CartItem> items, Double quantity, Double totalPrice) {
        this.customer = customer;
        this.items = items;
        this.orderDate = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.totalAmount = calculateTotal();
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        updateJavaFxProperties();
    }

    @PostLoad
    private void updateJavaFxProperties() {
        if (this.id != null) {
            this.idProperty.set(this.id);
        } else {
            this.idProperty.set(0L);
        }
        this.customerProperty.set(this.customer != null ? this.customer.getName() : "Guest");
        this.totalAmountProperty.set(this.totalAmount);
        this.orderDateProperty.set(this.orderDate != null ? this.orderDate.toString() : "");
    }

    private BigDecimal calculateTotal() {
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return items.stream()
                .map(CartItem::getTotalPrice)
                .filter(price -> price != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void addProduct(Product product, Integer quantity, Double totalPrice) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }

        CartItem item = new CartItem(this, product, quantity, totalPrice);
        this.items.add(item);
        this.totalAmount = calculateTotal();
    }

    public void addProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        addProduct(product, 1, product.getPrice().doubleValue());
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
        this.idProperty.set(id);
    }

    public Customers getCustomer() {
        return customer;
    }

    public void setCustomer(Customers customer) {
        this.customer = customer;
        this.customerProperty.set(customer != null ? customer.getName() : "Guest");
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
        this.totalAmount = calculateTotal();
        this.totalAmountProperty.set(this.totalAmount);
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
        this.orderDateProperty.set(orderDate.toString());
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
        this.totalAmountProperty.set(totalAmount);
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public boolean isPago() {
        return pago;
    }

    public void setPago(boolean pago) {
        this.pago = pago;
    }

    public LocalDateTime getDataPagamento() {
        return dataPagamento;
    }

    public void setDataPagamento(LocalDateTime dataPagamento) {
        this.dataPagamento = dataPagamento;
    }


    public String getChave() {
        return chave;
    }

    public void setChave(String chave) {
        this.chave = chave;
    }

    // Propriedades JavaFX para UI
    public LongProperty idProperty() {
        return idProperty;
    }

    public StringProperty customerProperty() {
        return customerProperty;
    }

    public ObjectProperty<BigDecimal> totalAmountProperty() {
        return totalAmountProperty;
    }

    public StringProperty orderDateProperty() {
        return orderDateProperty;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public String getDocumentoConsumidor() {
        return documentoConsumidor;
    }

    public void setDocumentoConsumidor(String documentoConsumidor) {
        this.documentoConsumidor = documentoConsumidor;
    }
    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", customer=" + (customer != null ? customer.getId() : "N/A") +
                ", items=" + items.size() +
                ", orderDate=" + orderDate +
                ", totalAmount=" + totalAmount +
                '}';
    }
}