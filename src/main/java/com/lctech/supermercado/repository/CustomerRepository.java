package com.lctech.supermercado.repository;

import com.lctech.supermercado.model.Customers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customers, Long> {

    // Busca com filtros opcionais e case-insensitive
    @Query("SELECT c FROM Customers c WHERE " +
            "(:name IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:email IS NULL OR LOWER(c.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
            "(:phone IS NULL OR c.phone LIKE CONCAT('%', :phone, '%'))")
    Page<Customers> searchCustomers(@Param("name") String name,
                                    @Param("email") String email,
                                    @Param("phone") String phone,
                                    Pageable pageable);

    // Busca por CPF
    @Query("SELECT c FROM Customers c WHERE c.cpf = :cpf")
    Optional<Customers> findByCpf(@Param("cpf") String cpf);

    // Busca por CNPJ
    @Query("SELECT c FROM Customers c WHERE c.cnpj = :cnpj")
    Optional<Customers> findByCnpj(@Param("cnpj") String cnpj);

    @Query("SELECT c FROM Customers c WHERE (:cpf IS NULL OR c.cpf = :cpf) AND (:cnpj IS NULL OR c.cnpj = :cnpj)")
    Optional<Customers> findByCpfOrCnpj(@Param("cpf") String cpf, @Param("cnpj") String cnpj);

    // Busca com filtros opcionais incluindo CPF e CNPJ
    @Query("SELECT c FROM Customers c WHERE " +
            "(:name IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:email IS NULL OR LOWER(c.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
            "(:phone IS NULL OR c.phone LIKE CONCAT('%', :phone, '%')) AND " +
            "(:cpf IS NULL OR c.cpf = :cpf) AND " +
            "(:cnpj IS NULL OR c.cnpj = :cnpj)")
    Page<Customers> advancedSearch(@Param("name") String name,
                                   @Param("email") String email,
                                   @Param("phone") String phone,
                                   @Param("cpf") String cpf,
                                   @Param("cnpj") String cnpj,
                                   Pageable pageable);
}
