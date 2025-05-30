package id.web.fitrarizki.ecommerce.service;

import id.web.fitrarizki.ecommerce.model.ActivityType;
import id.web.fitrarizki.ecommerce.model.UserActivity;

import java.time.LocalDateTime;
import java.util.List;

public interface UserActivityService {
    long getActivityCount(Long productId, ActivityType activityType);
    long getActivityCountInDateRange(Long productId, ActivityType activityType, LocalDateTime startDate, LocalDateTime endDate);
    void trackPurchase(Long productId, Long userId);
    void trackProductView(Long productId, Long userId);
    List<UserActivity> getLastMonthUserPurchase(Long userId);
    List<UserActivity> getLastMonthUserView(Long userId);
}
