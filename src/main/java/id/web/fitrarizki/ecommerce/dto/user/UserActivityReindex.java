package id.web.fitrarizki.ecommerce.dto.user;

import id.web.fitrarizki.ecommerce.model.ActivityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class UserActivityReindex implements Serializable {
    private Long userId;
    private Long productId;
    private ActivityType activityType;
}
