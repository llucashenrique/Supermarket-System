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
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Customer name is required.")
    @JsonProperty("nome_cliente")
    private String name;

    @NotNull(message = "Email is required.")
    @JsonProperty("email_cliente")
    private String email;

    @NotNull(message = "Phone is required.")
    @JsonProperty("telefone_cliente")
    private String phone; // Este é o atributo "phone"

    @NotNull(message = "Address is required.")
    @JsonProperty("endereco_cliente")
    private String address;
}


