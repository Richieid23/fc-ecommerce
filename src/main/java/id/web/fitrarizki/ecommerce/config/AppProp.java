package id.web.fitrarizki.ecommerce.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties("app")
@Setter
@Getter
public class AppProp {
    private JWT jwt;
    private Xendit xendit;
    private RateLimit rateLimit;

    @Setter
    @Getter
    public static class JWT {
        private String secretKey;
        private Duration expirationTime;
    }

    @Setter
    @Getter
    public static class Xendit {
        private String secretKey;
        private String publicKey;
    }

    @Setter
    @Getter
    public static class RateLimit {
        private int defaultLimit = 100;
        private int limitRefreshPeriod = 60;
        private int timeout = 1;
    }
}
