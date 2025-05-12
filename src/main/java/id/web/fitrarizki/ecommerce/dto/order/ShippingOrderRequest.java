package id.web.fitrarizki.ecommerce.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShippingOrderRequest {
    private Long orderId;
    private Address fromAddress;
    private Address toAddress;
    private BigDecimal totalWeightInGrams;

    @Data
    @Builder
    public static class Address {

        private String streetAddress;
        private String city;
        private String state;
        private String postalCode;
    }
}
