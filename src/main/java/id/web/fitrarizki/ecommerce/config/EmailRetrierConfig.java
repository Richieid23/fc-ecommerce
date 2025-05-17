package id.web.fitrarizki.ecommerce.config;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
@RequiredArgsConstructor
public class EmailRetrierConfig {

    private final AppProp appProp;

    @Bean
    public Retry emailRetrier() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(appProp.getSendgrid().getRetrier().getMaxAttempt())
                .waitDuration(appProp.getSendgrid().getRetrier().getWaitDuration())
                .retryExceptions(IOException.class)
                .build();

        return Retry.of("emailRetrier", config);
    }
}
