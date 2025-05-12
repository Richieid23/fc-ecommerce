package id.web.fitrarizki.ecommerce.service.impl;

import id.web.fitrarizki.ecommerce.dto.order.ShippingOrderRequest;
import id.web.fitrarizki.ecommerce.dto.order.ShippingOrderResponse;
import id.web.fitrarizki.ecommerce.dto.order.ShippingRateRequest;
import id.web.fitrarizki.ecommerce.dto.order.ShippingRateResponse;
import id.web.fitrarizki.ecommerce.exception.ResourceNotFoundException;
import id.web.fitrarizki.ecommerce.model.Order;
import id.web.fitrarizki.ecommerce.model.OrderItem;
import id.web.fitrarizki.ecommerce.model.Product;
import id.web.fitrarizki.ecommerce.repository.OrderItemRepository;
import id.web.fitrarizki.ecommerce.repository.OrderRepository;
import id.web.fitrarizki.ecommerce.repository.ProductRepository;
import id.web.fitrarizki.ecommerce.service.ShippingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MockShippingServiceImpl implements ShippingService {

    private static final BigDecimal BASE_RATE = BigDecimal.valueOf(10000);
    private static final BigDecimal RATE_PER_KG = BigDecimal.valueOf(2500);

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;

    @Override
    public ShippingRateResponse calculateShippingRate(ShippingRateRequest request) {
        // shipping_fee = base_rate + (weight * rate_per_kg)
        BigDecimal shippingFee = BASE_RATE.add(request.getTotalWeightInGrams().divide(BigDecimal.valueOf(1000)).multiply(RATE_PER_KG)).setScale(2, RoundingMode.HALF_UP);

        String estimatedDeliveryTime = "3 - 5 hari kerja";

        return ShippingRateResponse.builder()
                .shippingFee(shippingFee)
                .estimatedDeliveryTime(estimatedDeliveryTime)
                .build();
    }

    @Override
    public ShippingOrderResponse createShippingOrder(ShippingOrderRequest request) {
        String awbNumber = generateAwbNumber(request.getOrderId());

        Order order = orderRepository.findById(request.getOrderId()).orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        order.setStatus("SHIPPING");
        order.setAwbNumber(awbNumber);
        orderRepository.save(order);

        String estimatedDeliveryTime = "3 - 5 hari kerja";

        return ShippingOrderResponse.builder()
                .awbNumber(awbNumber)
                .estimatedDeliveryTime(estimatedDeliveryTime)
                .build();
    }

    @Override
    public String generateAwbNumber(Long orderId) {
        Random random = new Random();
        String prefix = "AWB";
        return String.format("%s%011d", prefix, random.nextInt(100000000));
    }

    @Override
    public BigDecimal calculateTotalWeight(Long orderId) {
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);

        List<Long> productIds = orderItems.stream().map(OrderItem::getProductId).toList();
        List<Product> products = productRepository.findAllById(productIds);
        Map<Long, Product> productMap = products.stream().collect(Collectors.toMap(Product::getId, Function.identity()));

        return orderItems.stream().map(orderItem -> {
            Product product = productMap.get(orderItem.getProductId());
            if (product == null) {
                throw new ResourceNotFoundException("Product not found");
            }

            return product.getWeight().multiply(BigDecimal.valueOf(orderItem.getQuantity()));
        }).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
