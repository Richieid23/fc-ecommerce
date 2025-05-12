package id.web.fitrarizki.ecommerce.controller;

import id.web.fitrarizki.ecommerce.dto.order.ShippingOrderRequest;
import id.web.fitrarizki.ecommerce.dto.order.ShippingOrderResponse;
import id.web.fitrarizki.ecommerce.dto.order.ShippingRateRequest;
import id.web.fitrarizki.ecommerce.dto.order.ShippingRateResponse;
import id.web.fitrarizki.ecommerce.service.ShippingService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("shippings")
@SecurityRequirement(name = "Bearer")
@RequiredArgsConstructor
public class ShippingController {
    private final ShippingService shippingService;

    @PostMapping("/rate")
    public ResponseEntity<ShippingRateResponse> calculateShippingRate(@Valid @RequestBody ShippingRateRequest shippingRateRequest) {
        ShippingRateResponse response = shippingService.calculateShippingRate(shippingRateRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/order")
    public ResponseEntity<ShippingOrderResponse> createShippingOrder(@Valid @RequestBody ShippingOrderRequest shippingOrderRequest) {
        ShippingOrderResponse response = shippingService.createShippingOrder(shippingOrderRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/awb/{orderId}")
    public ResponseEntity<String> generateAwbNumber(@PathVariable Long orderId) {
        String awbNumber = shippingService.generateAwbNumber(orderId);
        return ResponseEntity.ok(awbNumber);
    }

    @GetMapping("/weight/{orderId}")
    public ResponseEntity<BigDecimal> calculateTotalWeight(@PathVariable Long orderId) {
        BigDecimal totalWeight = shippingService.calculateTotalWeight(orderId);
        return ResponseEntity.ok(totalWeight);
    }
}
