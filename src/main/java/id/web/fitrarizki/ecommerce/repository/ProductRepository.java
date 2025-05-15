package id.web.fitrarizki.ecommerce.repository;

import id.web.fitrarizki.ecommerce.model.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query(value = """
        SELECT * FROM products
        WHERE lower("name") like :name
        """, nativeQuery = true)
    Page<Product> findByNameLike(String name, Pageable pageable);

    @Query(value = """
        SELECT DISTINCT p.* FROM products p 
        JOIN products_categories pc ON pc.category_id = p.id
        JOIN categories c on c.id = pc.category_id
        WHERE c.name = :categoryName
        """, nativeQuery = true)
    List<Product> findByCategoryName(@Param("categoryName") String categoryName);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = """
        SELECT * FROM products
        WHERE id = :id
    """, nativeQuery = true)
    Optional<Product> findByIdWithPesimisticLock(Long id);
}
