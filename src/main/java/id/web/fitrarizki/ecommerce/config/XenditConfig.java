package id.web.fitrarizki.ecommerce.config;

import com.xendit.Xendit;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class XenditConfig {
    private final AppProp appProp;

    @Bean
    public Xendit xenditClient() {
        Xendit.apiKey = appProp.getXendit().getSecretKey();
        return new Xendit();
    }
}
