package id.web.fitrarizki.ecommerce.dto.order;

import id.web.fitrarizki.ecommerce.model.UserAddress;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShippingRateRequest {
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

    public static Address fromUserAddress(UserAddress userAddress) {
        return Address.builder()
                .streetAddress(userAddress.getStreetAddress())
                .city(userAddress.getCity())
                .state(userAddress.getState())
                .postalCode(userAddress.getPostalCode())
                .build();
    }
}
