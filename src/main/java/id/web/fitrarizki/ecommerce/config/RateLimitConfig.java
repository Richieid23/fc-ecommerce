package id.web.fitrarizki.ecommerce.config;

import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class RateLimitConfig {

    private final AppProp appProp;

    @Bean
    public RateLimiterConfig rateLimiterConfig() {
        return RateLimiterConfig.custom()
                .limitForPeriod(appProp.getRateLimit().getDefaultLimit())
                .limitRefreshPeriod(Duration.ofSeconds(appProp.getRateLimit().getLimitRefreshPeriod()))
                .timeoutDuration(Duration.ofSeconds(appProp.getRateLimit().getTimeout()))
                .build();
    }

    @Bean
    public RateLimiterRegistry rateLimiterRegistry(RateLimiterConfig rateLimiterConfig) {
        return RateLimiterRegistry.of(rateLimiterConfig);
    }
}
