package id.web.fitrarizki.ecommerce.service.impl;

import com.xendit.exception.XenditException;
import com.xendit.model.Invoice;
import id.web.fitrarizki.ecommerce.dto.PaginatedResponse;
import id.web.fitrarizki.ecommerce.dto.order.*;
import id.web.fitrarizki.ecommerce.dto.payment.PaymentResponse;
import id.web.fitrarizki.ecommerce.exception.InventoryException;
import id.web.fitrarizki.ecommerce.exception.ResourceNotFoundException;
import id.web.fitrarizki.ecommerce.model.*;
import id.web.fitrarizki.ecommerce.repository.*;
import id.web.fitrarizki.ecommerce.service.InventoryService;
import id.web.fitrarizki.ecommerce.service.OrderService;
import id.web.fitrarizki.ecommerce.service.PaymentService;
import id.web.fitrarizki.ecommerce.service.ShippingService;
import id.web.fitrarizki.ecommerce.util.OrderStateTransitionUtil;
import id.web.fitrarizki.ecommerce.util.PageUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;
    private final UserAddressRepository userAddressRepository;
    private final ProductRepository productRepository;
    private final ShippingService mockShippingService;
    private final PaymentService paymentService;
    private final InventoryService inventoryService;

    private final BigDecimal TAX_RATE = BigDecimal.valueOf(0.11);

    @Override
    @Transactional
    public OrderResponse checkout(CheckOutRequest checkOutRequest) {
        List<CartItem> selectedItems = cartItemRepository.findAllById(checkOutRequest.getCartItemIds());
        if (selectedItems.isEmpty()) {
            throw new ResourceNotFoundException("Cart item not found");
        }

        UserAddress shippingAddress = userAddressRepository.findById(checkOutRequest.getUserAddressId()).orElseThrow(() -> new ResourceNotFoundException("Shipping address not found"));

        Map<Long, Integer> productQuantities = selectedItems.stream().collect(Collectors.toMap(CartItem::getProductId, CartItem::getQuantity));
        if (!inventoryService.checkInventoryAvailability(productQuantities)) {
            throw new InventoryException("Inventory not available");
        }

        Order order = Order.builder()
                .userId(checkOutRequest.getUserId())
                .status(OrderStatus.PENDING)
                .orderDate(LocalDateTime.now())
                .totalAmount(BigDecimal.ZERO)
                .taxFee(BigDecimal.ZERO)
                .subtotal(BigDecimal.ZERO)
                .shippingFee(BigDecimal.ZERO)
                .build();

        Order savedOrder = orderRepository.save(order);

        List<OrderItem> orderItems = selectedItems.stream().map(cartItem -> OrderItem.builder()
                .orderId(savedOrder.getId())
                .productId(cartItem.getProductId())
                .quantity(cartItem.getQuantity())
                .price(cartItem.getPrice())
                .userAddressId(shippingAddress.getId())
                .build()).toList();

        orderItemRepository.saveAll(orderItems);
        cartItemRepository.deleteAll(selectedItems);

        BigDecimal subTotal = orderItems.stream().map(orderItem -> orderItem.getPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity()))).reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Long> productIds = orderItems.stream().map(OrderItem::getProductId).toList();
        List<Product> products = productRepository.findAllById(productIds);
        Map<Long, Product> productMap = products.stream().collect(Collectors.toMap(Product::getId, Function.identity()));

        BigDecimal shippingFee = orderItems.stream().map(orderItem -> {
            Product product = productMap.get(orderItem.getProductId());
            if (product == null) {
                return BigDecimal.ZERO;
            }

            Optional<UserAddress> sellerAddress = userAddressRepository.findByUserIdAndIsDefaultTrue(product.getUserId());
            if (sellerAddress.isEmpty()) {
                return BigDecimal.ZERO;
            }

            BigDecimal totalWeight = product.getWeight().multiply(BigDecimal.valueOf(orderItem.getQuantity()));

            // calculate shipping fee
            ShippingRateRequest rateRequest = ShippingRateRequest.builder()
                    .totalWeightInGrams(totalWeight)
                    .fromAddress(ShippingRateRequest.fromUserAddress(sellerAddress.get()))
                    .toAddress(ShippingRateRequest.fromUserAddress(shippingAddress))
                    .build();
            ShippingRateResponse rateResponse = mockShippingService.calculateShippingRate(rateRequest);
            return rateResponse.getShippingFee();
        }).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal taxFee = subTotal.multiply(TAX_RATE);
        BigDecimal totalAmount = subTotal.add(shippingFee).add(taxFee);

        savedOrder.setSubtotal(subTotal);
        savedOrder.setShippingFee(shippingFee);
        savedOrder.setTaxFee(taxFee);
        savedOrder.setTotalAmount(totalAmount);

        // interact with xendit API
        // generate payment url
        String paymentUrl = "";

        try {
            PaymentResponse paymentResponse = paymentService.create(savedOrder);
            savedOrder.setXenditInvoiceId(paymentResponse.getXenditInvoiceId());
            savedOrder.setXenditPaymentMethod(paymentResponse.getXenditPaymentMethod());
            savedOrder.setXenditPaymentStatus(paymentResponse.getXenditInvoiceStatus());
            paymentUrl = paymentResponse.getXenditPaymentUrl();

        } catch (Exception e) {
            log.error("Payment failed for order {}, message: {}", savedOrder.getId(), e.getMessage());
            savedOrder.setStatus(OrderStatus.PAYMENT_FAILED);
        }

        if (!savedOrder.getStatus().equals(OrderStatus.PAYMENT_FAILED)) {
            inventoryService.decreaseInventory(productQuantities);
        }

        orderRepository.save(savedOrder);

        OrderResponse orderResponse = OrderResponse.fromOrder(savedOrder);
        orderResponse.setPaymentUrl(paymentUrl);
        return orderResponse;
    }

    @Override
    public Order getOrderById(Long id) {
        return orderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }

    @Override
    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    @Override
    public PaginatedResponse<OrderResponse> getPaginatedOrdersByUserId(Long userId, Pageable pageable) {
        Page<OrderResponse> orderPage = orderRepository.findByUserId(userId, pageable).map(OrderResponse::fromOrder);
        return PageUtil.getPaginatedResponse(orderPage);
    }

    @Override
    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    @Override
    @Transactional
    public void cancelOrder(Long id) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!OrderStateTransitionUtil.isValidTransition(order.getStatus(), OrderStatus.CANCELLED)) {
            throw new IllegalStateException("Cannot cancel order that is not in pending state");
        }

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
        Map<Long, Integer> productQuantities = orderItems.stream().collect(Collectors.toMap(OrderItem::getProductId, OrderItem::getQuantity));
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        cancelXenditInvoice(order);
        inventoryService.increaseInventory(productQuantities);
    }

    @Override
    public List<OrderItemResponse> getOrderItemsByOrderId(Long orderId) {
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        if (orderItems.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> productIds = orderItems.stream().map(OrderItem::getProductId).toList();
        List<Product> products = productRepository.findAllById(productIds);
        Map<Long, Product> productMap = products.stream().collect(Collectors.toMap(Product::getId, Function.identity()));

        List<Long> userAddressIds = orderItems.stream().map(OrderItem::getUserAddressId).toList();
        List<UserAddress> userAddresses = userAddressRepository.findAllById(userAddressIds);
        Map<Long, UserAddress> userAddressMap = userAddresses.stream().collect(Collectors.toMap(UserAddress::getId, Function.identity()));

        return orderItems.stream().map(orderItem -> {
            Product product = productMap.get(orderItem.getProductId());
            UserAddress userAddress = userAddressMap.get(orderItem.getUserAddressId());

            if (product == null) {
                throw new ResourceNotFoundException("Product not found");
            }

            if (userAddress ==  null) {
                throw new ResourceNotFoundException("User address not found");
            }

            return OrderItemResponse.fromOrderItemProductAndAddress(orderItem, product, userAddress);
        }).toList();
    }

    @Override
    @Transactional
    public void updateOrderStatus(Long id, OrderStatus status) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!OrderStateTransitionUtil.isValidTransition(order.getStatus(), status)) {
            throw new IllegalStateException("Cannot update orders state");
        }

        order.setStatus(status);
        orderRepository.save(order);

        if (status.equals(OrderStatus.CANCELLED)) {
            cancelXenditInvoice(order);
            List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
            Map<Long, Integer> productQuantities = orderItems.stream().collect(Collectors.toMap(OrderItem::getProductId, OrderItem::getQuantity));
            inventoryService.increaseInventory(productQuantities);
        }
    }

    @Override
    public BigDecimal calculateOrderTotal(Long orderId) {
        return orderItemRepository.calculateTotalPrice(orderId);
    }

    private void cancelXenditInvoice(Order order) {
        try {
            Invoice invoice = Invoice.expire(order.getXenditInvoiceId());
            order.setXenditPaymentStatus(invoice.getStatus());
            orderRepository.save(order);
        } catch (XenditException e) {
            log.error("Xendit error: {}", e.getMessage());
        }
    }

    // run every minute
    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void cancelUnpaidOrders() {
        LocalDateTime cancelThreshold = LocalDateTime.now().minusDays(1);
        List<Order> unpaidOrders = orderRepository.findByStatusAndOrderDateBefore(OrderStatus.PENDING, cancelThreshold);

        for (Order unpaidOrder : unpaidOrders) {
            unpaidOrder.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(unpaidOrder);
            cancelXenditInvoice(unpaidOrder);
        }
    }
}
