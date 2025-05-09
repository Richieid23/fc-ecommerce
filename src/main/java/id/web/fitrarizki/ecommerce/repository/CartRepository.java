package id.web.fitrarizki.ecommerce.repository;

import id.web.fitrarizki.ecommerce.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    boolean existsByUserId(Long userId);
    Optional<Cart> findByUserId(Long userId);
}
