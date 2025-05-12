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
public class ShippingRateResponse {
    private BigDecimal shippingFee;
    private String estimatedDeliveryTime;
}
