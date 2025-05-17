package id.web.fitrarizki.ecommerce.config;

import com.sendgrid.SendGrid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class SendgridConfig {

    private final AppProp appProp;

    @Bean
    public SendGrid sendGridConfig() {
        return new SendGrid(appProp.getSendgrid().getApiKey());
    }
}
