package id.web.fitrarizki.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FcEcommerceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FcEcommerceApplication.class, args);
    }

}
