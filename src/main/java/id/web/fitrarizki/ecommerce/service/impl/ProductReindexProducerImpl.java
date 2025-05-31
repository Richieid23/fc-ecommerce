package id.web.fitrarizki.ecommerce.service.impl;

import id.web.fitrarizki.ecommerce.config.AppProp;
import id.web.fitrarizki.ecommerce.dto.product.ProductReindex;
import id.web.fitrarizki.ecommerce.service.ProductReindexProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductReindexProducerImpl implements ProductReindexProducer {
    private final KafkaTemplate<String, ProductReindex> kafkaTemplate;
    private final AppProp appProp;

    @Override
    public void publishProductReindex(ProductReindex message) {
        kafkaTemplate.send(appProp.getKafka().getTopic().getProductReindexName(), message);
    }
}
