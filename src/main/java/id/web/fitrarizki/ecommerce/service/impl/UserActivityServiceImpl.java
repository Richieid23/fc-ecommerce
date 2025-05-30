package id.web.fitrarizki.ecommerce.service.impl;

import id.web.fitrarizki.ecommerce.model.ActivityType;
import id.web.fitrarizki.ecommerce.model.UserActivity;
import id.web.fitrarizki.ecommerce.repository.UserActivityRepository;
import id.web.fitrarizki.ecommerce.service.ProductIndexService;
import id.web.fitrarizki.ecommerce.service.UserActivityService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserActivityServiceImpl implements UserActivityService {
    private final UserActivityRepository userActivityRepository;
    private final ProductIndexService productIndexService;

    @Override
    public long getActivityCount(Long productId, ActivityType activityType) {
        return userActivityRepository.countByProductIdAndActivityType(productId, activityType);
    }

    @Override
    public long getActivityCountInDateRange(Long productId, ActivityType activityType, LocalDateTime startDate, LocalDateTime endDate) {
        return userActivityRepository.countByProductIdAndActivityTypeAndCreatedAtBetween(productId, activityType, startDate, endDate);
    }

    @Override
    @Async
    @Transactional
    public void trackPurchase(Long productId, Long userId) {
        UserActivity userActivity = UserActivity.builder()
                .productId(productId)
                .userId(userId)
                .activityType(ActivityType.PURCHASE)
                .build();
        userActivityRepository.save(userActivity);

        Long purchaseCount = getActivityCount(productId, ActivityType.PURCHASE);
        productIndexService.reindexProductActivity(productId, ActivityType.PURCHASE, purchaseCount);
    }

    @Override
    @Async
    @Transactional
    public void trackProductView(Long productId, Long userId) {
        UserActivity userActivity = UserActivity.builder()
                .productId(productId)
                .userId(userId)
                .activityType(ActivityType.VIEW)
                .build();
        userActivityRepository.save(userActivity);

        Long viewCount = getActivityCount(productId, ActivityType.VIEW);
        productIndexService.reindexProductActivity(productId, ActivityType.VIEW, viewCount);
    }

    @Override
    public List<UserActivity> getLastMonthUserPurchase(Long userId) {
        LocalDateTime startDate = LocalDateTime.now().minusMonths(1);
        LocalDateTime endDate = LocalDateTime.now();

        return userActivityRepository.findByUserIdAndActivityTypeAndCreatedAtBetween(userId, ActivityType.PURCHASE, startDate, endDate);
    }

    @Override
    public List<UserActivity> getLastMonthUserView(Long userId) {
        LocalDateTime startDate = LocalDateTime.now().minusMonths(1);
        LocalDateTime endDate = LocalDateTime.now();

        return userActivityRepository.findByUserIdAndActivityTypeAndCreatedAtBetween(userId, ActivityType.VIEW, startDate, endDate);
    }
}
