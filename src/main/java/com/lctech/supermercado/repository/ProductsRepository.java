package com.lctech.supermercado.repository;

import com.lctech.supermercado.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductsRepository extends JpaRepository<Product, Long> {

    /**
     * Busca produtos que contenham o nome especificado, ignorando maiúsculas e minúsculas.
     *
     * @param name Parte do nome do produto.
     * @return Lista de produtos correspondentes.
     */
    List<Product> findByNameContainingIgnoreCase(String name);

    /**
     * Pesquisa avançada de produtos com filtros opcionais.
     *
     * @param name     Parte do nome do produto (opcional).
     * @param minPrice Preço mínimo (opcional).
     * @param maxPrice Preço máximo (opcional).
     * @param minStock Estoque mínimo (opcional).
     * @param maxStock Estoque máximo (opcional).
     * @return Lista de produtos que correspondem aos critérios de pesquisa.
     */
    @Query("SELECT p FROM Product p WHERE " +
            "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
            "(:minStock IS NULL OR p.stock >= :minStock) AND " +
            "(:maxStock IS NULL OR p.stock <= :maxStock)")
    List<Product> searchProducts(@Param("name") String name,
                                 @Param("minPrice") Double minPrice,
                                 @Param("maxPrice") Double maxPrice,
                                 @Param("minStock") Double minStock,
                                 @Param("maxStock") Double maxStock);

    /**
     * Atualiza o estoque de um produto com base no ID.
     *
     * @param id    ID do produto.
     * @param stock Novo valor do estoque.
     */
    @Modifying
    @Query("UPDATE Product p SET p.stock = :stock WHERE p.id = :id")
    void updateStock(@Param("id") Long id, @Param("stock") Double stock);

    /**
     * Atualiza o tipo de um produto com base no ID.
     *
     * @param id   ID do produto.
     * @param tipo Novo tipo do produto (ex.: kg, unid, dz).
     */
    @Modifying
    @Query("UPDATE Product p SET p.tipo = :tipo WHERE p.id = :id")
    void updateProductType(@Param("id") Long id, @Param("tipo") String tipo);
}
