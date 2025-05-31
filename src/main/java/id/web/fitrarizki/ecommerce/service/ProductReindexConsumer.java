package id.web.fitrarizki.ecommerce.service;

import id.web.fitrarizki.ecommerce.dto.product.ProductReindex;

public interface ProductReindexConsumer {
    void listen(ProductReindex message);
}
