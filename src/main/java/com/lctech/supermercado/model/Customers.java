package com.lctech.supermercado.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "customer", indexes = {
        @Index(name = "idx_customer_name", columnList = "name"),
        @Index(name = "idx_customer_email", columnList = "email"),
        @Index(name = "idx_customer_phone", columnList = "phone"),
        @Index(name = "idx_customer_cpf", columnList = "cpf"),
        @Index(name = "idx_customer_cnpj", columnList = "cnpj")
})
public class Customers {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Customer name is required.")
    @JsonProperty("customer_name")
    private String name;

    @NotNull(message = "Email is required.")
    @Email(message = "Invalid email format.")
    @JsonProperty("customer_email")
    @Column(unique = true) // Garantir unicidade no banco
    private String email;

    @NotNull(message = "Phone is required.")
    @Pattern(regexp = "\\d{10,15}", message = "Phone number must be between 10 and 15 digits.")
    @JsonProperty("customer_phone")
    @Column(unique = true) // Garantir unicidade no banco
    private String phone;

    @NotNull(message = "Address is required.")
    @JsonProperty("customer_address")
    private String address;

    @Pattern(regexp = "\\d{11}", message = "CPF must have 11 digits.")
    @Column(unique = true)
    private String cpf;

    @Pattern(regexp = "\\d{14}", message = "CNPJ must have 14 digits.")
    @Column(unique = true)
    private String cnpj;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Transient JavaFX property for `name`
    @Transient
    private final StringProperty nameProperty = new SimpleStringProperty();

    public Customers(Long id, String name, String email, String phone) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    // Sync JavaFX property with JPA property
    public void updateJavaFxProperties() {
        this.nameProperty.set(this.name);
    }

    // Getter for nameProperty
    public StringProperty nameProperty() {
        return nameProperty;
    }

    // Override setters to update JavaFX property
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.nameProperty.set(name);
    }

    // Getters and setters for other fields
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return this.name; // Ou getName(), dependendo do nome do atributo
    }

}


