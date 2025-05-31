package id.web.fitrarizki.ecommerce.service;

import id.web.fitrarizki.ecommerce.dto.user.UserActivityReindex;

public interface UserActivityReindexProducer {
    void publishUserActivityReindex(UserActivityReindex message);
}
