package id.web.fitrarizki.ecommerce.repository;

import id.web.fitrarizki.ecommerce.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query(value = """
        SELECT * FROM product
        WHERE lower("name") like :name
        """, nativeQuery = true)
    Page<Product> findByNameLike(String name, Pageable pageable);

    @Query(value = """
        SELECT DISTINCT p.* FROM product p 
        JOIN product_category pc ON pc.category_id = p.id
        JOIN category c on c.id = pc.category_id
        WHERE c.name = :categoryName
        """, nativeQuery = true)
    List<Product> findByCategoryName(@Param("categoryName") String categoryName);
}
