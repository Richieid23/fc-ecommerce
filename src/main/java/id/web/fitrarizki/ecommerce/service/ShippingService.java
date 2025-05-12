package id.web.fitrarizki.ecommerce.service;

import id.web.fitrarizki.ecommerce.dto.order.ShippingOrderRequest;
import id.web.fitrarizki.ecommerce.dto.order.ShippingOrderResponse;
import id.web.fitrarizki.ecommerce.dto.order.ShippingRateRequest;
import id.web.fitrarizki.ecommerce.dto.order.ShippingRateResponse;

import java.math.BigDecimal;

public interface ShippingService {

    ShippingRateResponse calculateShippingRate(ShippingRateRequest request);

    ShippingOrderResponse createShippingOrder(ShippingOrderRequest request);

    String generateAwbNumber(Long orderId);

    BigDecimal calculateTotalWeight(Long orderId);
}
