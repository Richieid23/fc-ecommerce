package id.web.fitrarizki.ecommerce.repository;

import id.web.fitrarizki.ecommerce.model.ActivityType;
import id.web.fitrarizki.ecommerce.model.UserActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface UserActivityRepository extends JpaRepository<UserActivity, Long> {
    long countByProductIdAndActivityType(long productId, ActivityType activityType);
    long countByProductIdAndActivityTypeAndCreatedAtBetween(long productId, ActivityType activityType, LocalDateTime startDate, LocalDateTime endDate);
    List<UserActivity> findByUserIdAndActivityTypeAndCreatedAtBetween(long userId, ActivityType activityType, LocalDateTime startDate, LocalDateTime endDate);
}
