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

    @Setter
    @Getter
    public static class JWT {
        private String secretKey;
        private Duration expirationTime;
    }
}
