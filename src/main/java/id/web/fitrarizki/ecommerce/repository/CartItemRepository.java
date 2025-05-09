package id.web.fitrarizki.ecommerce.repository;

import id.web.fitrarizki.ecommerce.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    @Query(value = """
        SELECT ci.* FROM cart_items ci
        JOIN carts c ON c.id = ci.cart_id
        WHERE c.user_id = :userId
    """, nativeQuery = true)
    List<CartItem> findByUserId(Long userId);

    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);

    void deleteAllByCartId(Long cartId);
}
