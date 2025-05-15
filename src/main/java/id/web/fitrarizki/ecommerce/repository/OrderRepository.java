package id.web.fitrarizki.ecommerce.repository;

import id.web.fitrarizki.ecommerce.model.Order;
import id.web.fitrarizki.ecommerce.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);

    Page<Order> findByUserId(Long userId, Pageable pageable);
    List<Order> findByStatus(OrderStatus status);
    List<Order> findByUserIdAndOrderDateBetween(Long userId, LocalDateTime startDate, LocalDateTime endDate);

    Optional<Order> findByXenditInvoiceId(String xenditInvoiceId);

    List<Order> findByStatusAndOrderDateBefore(OrderStatus status, LocalDateTime date);
}
