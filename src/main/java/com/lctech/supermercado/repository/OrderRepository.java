package com.lctech.supermercado.repository;

import com.lctech.supermercado.model.Customers;
import com.lctech.supermercado.model.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :id")
    Order buscarComItens(@Param("id") Long id);

    // Buscar todos os pedidos de um cliente pelo objeto
    List<Order> findByCustomer(Customers customer);

    // Buscar todos os pedidos de um cliente pelo ID (alternativo)
    List<Order> findByCustomerId(Long customerId);

    // Buscar pedidos com os itens já carregados (evita LazyInitializationException)
    @Query("SELECT o FROM Order o JOIN FETCH o.items")
    List<Order> findAllWithItems();

    // Calcular o total de todas as compras de um cliente
    @Query("SELECT SUM(o.totalPrice) FROM Order o WHERE o.customer.id = :customerId")
    Optional<Double> calculateTotalByCustomerId(@Param("customerId") Long customerId);

    // Produtos mais vendidos (estatística)
    @Query("SELECT i.product.id, i.product.name, SUM(i.quantity) AS totalSold " +
            "FROM Order o JOIN o.items i " +
            "GROUP BY i.product.id, i.product.name " +
            "ORDER BY totalSold DESC")
    List<Object[]> findTopProducts();

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE FUNCTION('MONTH', o.orderDate) = :month AND FUNCTION('YEAR', o.orderDate) = :year")
    Double getMonthlyTotalByMonthAndYear(@Param("month") int month, @Param("year") int year);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.paymentType = :paymentType AND FUNCTION('MONTH', o.orderDate) = :month")
    Double sumByPaymentTypeAndMonth(@Param("paymentType") String type, @Param("month") int month);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt >= :start AND o.createdAt < :end")
    Long countOrdersByDate(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderDate >= :start AND o.orderDate < :end")
    Long countOrdersByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.orderDate BETWEEN :start AND :end")
    BigDecimal sumOrdersByDate(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT o.paymentType, SUM(o.totalAmount) FROM Order o WHERE FUNCTION('MONTH', o.orderDate) = :month GROUP BY o.paymentType")
    List<Object[]> sumTotalByPaymentTypeInMonth(@Param("month") int month);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.paymentType = :paymentType AND o.orderDate BETWEEN :start AND :end")
    BigDecimal sumByPaymentTypeAndDate(@Param("paymentType") String type, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE FUNCTION('MONTH', o.orderDate) = :month")
    BigDecimal sumMonthlyTotalByMonth(@Param("month") int month);

    List<Order> findByOrderDateBetween(LocalDateTime start, LocalDateTime end);


    // (Opcional) Pedidos SEM cliente (fiado não identificado)
    List<Order> findByCustomerIsNull();
}






