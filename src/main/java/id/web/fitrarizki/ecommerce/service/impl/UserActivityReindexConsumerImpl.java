package id.web.fitrarizki.ecommerce.service.impl;

import id.web.fitrarizki.ecommerce.dto.user.UserActivityReindex;
import id.web.fitrarizki.ecommerce.model.ActivityType;
import id.web.fitrarizki.ecommerce.service.UserActivityReindexConsumer;
import id.web.fitrarizki.ecommerce.service.UserActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserActivityReindexConsumerImpl implements UserActivityReindexConsumer {
    private final UserActivityService userActivityService;

    @Override
    @KafkaListener(topics = "${app.kafka.topic.user-activity-reindex-name}", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(UserActivityReindex message) {
        if (!List.of(ActivityType.PURCHASE, ActivityType.VIEW).contains(message.getActivityType())) {
            return;
        }

        if (message.getProductId() == null || message.getUserId() == null) {
            return;
        }

        if (message.getActivityType().equals(ActivityType.PURCHASE)) {
            userActivityService.trackPurchase(message.getProductId(), message.getUserId());
            return;
        }

        if (message.getActivityType().equals(ActivityType.VIEW)) {
            userActivityService.trackProductView(message.getProductId(), message.getUserId());
            return;
        }
    }
}
