package id.web.fitrarizki.ecommerce.repository;

import id.web.fitrarizki.ecommerce.model.ProductCategory;
import id.web.fitrarizki.ecommerce.model.ProductCategory.ProductCategoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, ProductCategoryId> {

    @Query(value = """
        SELECT * FROM products_categories
        WHERE product_id = :productId
        """, nativeQuery = true)
    List<ProductCategory> findCategoriesByProductId(@Param("productId") Long productId);
}

