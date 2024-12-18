package com.lctech.supermercado.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import jakarta.validation.constraints.NotNull;
import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Product name is required.")
    @JsonProperty("nome_produto")
    private String name;

    @NotNull(message = "Barcode is required.")
    @JsonProperty("codigo_barras")
    private String barcode;

    @NotNull(message = "Price is required.")
    @JsonProperty("preco")
    private Double price;

    @NotNull(message = "Stock is required.")
    @JsonProperty("estoque")
    private Integer stock;
}
