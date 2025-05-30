package id.web.fitrarizki.ecommerce.service;

import id.web.fitrarizki.ecommerce.model.ActivityType;
import id.web.fitrarizki.ecommerce.model.Product;

public interface ProductIndexService {
    void reIndexProducts(Product product);
    void deleteProduct(Product product);
    String getProductIndexName();
    void reindexProductActivity(Long productId, ActivityType activityType, Long value);
}
