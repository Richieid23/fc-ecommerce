package id.web.fitrarizki.ecommerce.service.impl;

import id.web.fitrarizki.ecommerce.config.AppProp;
import id.web.fitrarizki.ecommerce.dto.user.UserActivityReindex;
import id.web.fitrarizki.ecommerce.service.UserActivityReindexProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserActivityReindexProducerImpl implements UserActivityReindexProducer {
    private final KafkaTemplate<String, UserActivityReindex> kafkaTemplate;
    private final AppProp appProp;

    @Override
    public void publishUserActivityReindex(UserActivityReindex message) {
        kafkaTemplate.send(appProp.getKafka().getTopic().getUserActivityReindexName(), message);
    }
}
