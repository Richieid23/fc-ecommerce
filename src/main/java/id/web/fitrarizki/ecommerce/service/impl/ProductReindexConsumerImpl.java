package id.web.fitrarizki.ecommerce.service.impl;

import id.web.fitrarizki.ecommerce.config.AppProp;
import id.web.fitrarizki.ecommerce.dto.product.ProductReindex;
import id.web.fitrarizki.ecommerce.model.Product;
import id.web.fitrarizki.ecommerce.service.ProductIndexService;
import id.web.fitrarizki.ecommerce.service.ProductReindexConsumer;
import id.web.fitrarizki.ecommerce.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductReindexConsumerImpl implements ProductReindexConsumer {

    private final ProductIndexService productIndexService;
    private final ProductService productService;
    private final AppProp appProp;

    @Override
    @KafkaListener(topics = "${app.kafka.topic.product-reindex-name}", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(ProductReindex message) {
        if (!List.of("REINDEX", "DELETE").contains(message.getAction())) {
            log.error("Invalid action: {}", message.getAction());
            return;
        }

        Product product = productService.get(message.getProductId());
        if (product == null) {
            log.error("Product not found: {}", message.getProductId());
            return;
        }

        if (message.getAction().equals("REINDEX")) {
            productIndexService.reIndexProducts(product);
        }

        if (message.getAction().equals("DELETE")) {
            productIndexService.deleteProduct(product);
        }
    }
}
