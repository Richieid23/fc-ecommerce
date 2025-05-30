package id.web.fitrarizki.ecommerce.config;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
@RequiredArgsConstructor
public class ElasticsearchIndexRetrierConfig {

    private final AppProp appProp;

    @Bean
    public Retry elasticsearchIndexRetrier() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(appProp.getElasticsearch().getIndexing().getRetrier().getMaxAttempt())
                .waitDuration(appProp.getElasticsearch().getIndexing().getRetrier().getWaitDuration())
                .retryExceptions(IOException.class)
                .build();

        return Retry.of("elasticsearchIndexRetrier", config);
    }
}
