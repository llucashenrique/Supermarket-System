package com.lctech.supermercado.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import javafx.beans.property.*;

import java.math.BigDecimal;

@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true)
    private Boolean balanca; // true se é controlado por balança, caso contrário false

    @Transient
    private final BooleanProperty balancaProperty = new SimpleBooleanProperty();

    @NotNull(message = "Product name is required.")
    @Size(min = 1, max = 100, message = "Product name must be between 1 and 100 characters.")
    private String name;


    @Pattern(regexp = "^$|\\d{8,13}", message = "Se informado, o código de barras deve conter entre 8 e 13 dígitos.")
    private String barcode;

    @NotNull(message = "Price is required.")
    @DecimalMin(value = "0.01", inclusive = true, message = "Price must be greater than or equal to 0.01.")
    private BigDecimal price;

    @NotNull(message = "Stock is required.")
    @PositiveOrZero(message = "Stock cannot be negative.")
    private Double stock;

    @NotNull(message = "NCM is required.")
    @Size(min = 8, max = 8, message = "NCM must be exactly 8 digits.")
    @Pattern(regexp = "\\d{8}", message = "NCM must contain exactly 8 numeric digits.")
    @Column(nullable = false, length = 8)
    private String ncm;

    @NotNull(message = "Product type is required.")
    @Size(min = 1, max = 50, message = "Product type must be between 1 and 50 characters.")
    private String tipo;

    // Transient properties for JavaFX integration
    @Transient
    private final StringProperty nameProperty = new SimpleStringProperty();

    @Transient
    private final StringProperty barcodeProperty = new SimpleStringProperty();

    @Transient
    private final ObjectProperty<BigDecimal> priceProperty = new SimpleObjectProperty<>();

    @Transient
    private final DoubleProperty stockProperty = new SimpleDoubleProperty(); // ✅ Alterado

    @Transient
    private final StringProperty ncmProperty = new SimpleStringProperty();

    @Transient
    private final StringProperty tipoProperty = new SimpleStringProperty();
    @Column(length = 1)

    private String origem;

    @Column(length = 3)
    private String cstIcms;

    @Column(precision = 5, scale = 2)
    private BigDecimal aliquotaIcms;

    @Column(length = 2)
    private String cstPis;

    @Column(precision = 5, scale = 2)
    private BigDecimal aliquotaPis;

    @Column(length = 2)
    private String cstCofins;

    @Column(precision = 5, scale = 2)
    private BigDecimal aliquotaCofins;

    @Column(length = 7)
    private String cest;

    private Boolean incluirNoTotal;
    // Default constructor
    public Product() {}

    // Parameterized constructor
    public Product(String name, String barcode, BigDecimal price, Double stock, String ncm, String tipo, Boolean balanca) {
        this.name = name;
        this.barcode = barcode;
        this.price = price;
        this.stock = stock;
        this.ncm = ncm;
        this.tipo = tipo;
        this.balanca = balanca;
        updateJavaFxProperties();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.nameProperty.set(name);
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
        this.barcodeProperty.set(barcode);
    }

    public boolean isBalanca() {
        return balanca != null && balanca;  // Verifica se balanca está true
    }

    public void setBalanca(Boolean balanca) {
        this.balanca = balanca;
        this.balancaProperty.set(balanca);
    }

    public BooleanProperty balancaProperty() {
        return balancaProperty;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
        this.priceProperty.set(price);
    }

    public Double getStock() {
        return stock;
    }

    public void setStock(Double stock) {
        this.stock = stock;
        this.stockProperty.set(stock);
    }

    public String getNcm() {
        return ncm;
    }

    public void setNcm(String ncm) {
        this.ncm = ncm;
        this.ncmProperty.set(ncm);
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
        this.tipoProperty.set(tipo);
    }

    // JavaFX Property Getters
    public StringProperty nameProperty() {
        return nameProperty;
    }

    public StringProperty barcodeProperty() {
        return barcodeProperty;
    }

    public ObjectProperty<BigDecimal> priceProperty() {
        return priceProperty;
    }

    public DoubleProperty stockProperty() { // ✅ Alterado
        return stockProperty;
    }

    public StringProperty ncmProperty() {
        return ncmProperty;
    }

    public StringProperty tipoProperty() {
        return tipoProperty;
    }

    public Boolean getBalanca() {
        return balanca;
    }

    public boolean isBalancaProperty() {
        return balancaProperty.get();
    }

    public BooleanProperty balancaPropertyProperty() {
        return balancaProperty;
    }

    public String getNameProperty() {
        return nameProperty.get();
    }

    public StringProperty namePropertyProperty() {
        return nameProperty;
    }

    public String getBarcodeProperty() {
        return barcodeProperty.get();
    }

    public StringProperty barcodePropertyProperty() {
        return barcodeProperty;
    }

    public BigDecimal getPriceProperty() {
        return priceProperty.get();
    }

    public ObjectProperty<BigDecimal> pricePropertyProperty() {
        return priceProperty;
    }

    public double getStockProperty() {
        return stockProperty.get();
    }

    public DoubleProperty stockPropertyProperty() {
        return stockProperty;
    }

    public String getNcmProperty() {
        return ncmProperty.get();
    }

    public StringProperty ncmPropertyProperty() {
        return ncmProperty;
    }

    public String getTipoProperty() {
        return tipoProperty.get();
    }

    public StringProperty tipoPropertyProperty() {
        return tipoProperty;
    }

    public String getOrigem() {
        return origem;
    }

    public void setOrigem(String origem) {
        this.origem = origem;
    }

    public String getCstIcms() {
        return cstIcms;
    }

    public void setCstIcms(String cstIcms) {
        this.cstIcms = cstIcms;
    }

    public BigDecimal getAliquotaIcms() {
        return aliquotaIcms;
    }

    public void setAliquotaIcms(BigDecimal aliquotaIcms) {
        this.aliquotaIcms = aliquotaIcms;
    }

    public String getCstPis() {
        return cstPis;
    }

    public void setCstPis(String cstPis) {
        this.cstPis = cstPis;
    }

    public BigDecimal getAliquotaPis() {
        return aliquotaPis;
    }

    public void setAliquotaPis(BigDecimal aliquotaPis) {
        this.aliquotaPis = aliquotaPis;
    }

    public String getCstCofins() {
        return cstCofins;
    }

    public void setCstCofins(String cstCofins) {
        this.cstCofins = cstCofins;
    }

    public BigDecimal getAliquotaCofins() {
        return aliquotaCofins;
    }

    public void setAliquotaCofins(BigDecimal aliquotaCofins) {
        this.aliquotaCofins = aliquotaCofins;
    }

    public String getCest() {
        return cest;
    }

    public void setCest(String cest) {
        this.cest = cest;
    }

    public Boolean getIncluirNoTotal() {
        return incluirNoTotal;
    }

    public void setIncluirNoTotal(Boolean incluirNoTotal) {
        this.incluirNoTotal = incluirNoTotal;
    }

    private void updateJavaFxProperties() {
        this.nameProperty.set(this.name);
        this.barcodeProperty.set(this.barcode);
        this.priceProperty.set(this.price);
        this.stockProperty.set(this.stock);
        this.ncmProperty.set(this.ncm);
        this.tipoProperty.set(this.tipo);
        this.balancaProperty.set(this.balanca);
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", barcode='" + barcode + '\'' +
                ", price=" + price +
                ", stock=" + stock +
                ", ncm='" + ncm + '\'' +
                ", tipo='" + tipo + '\'' +
                ", balanca=" + balanca +
                '}';
    }
}
