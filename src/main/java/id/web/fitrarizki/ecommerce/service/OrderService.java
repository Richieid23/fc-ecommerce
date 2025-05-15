package id.web.fitrarizki.ecommerce.service;

import id.web.fitrarizki.ecommerce.dto.PaginatedResponse;
import id.web.fitrarizki.ecommerce.dto.order.CheckOutRequest;
import id.web.fitrarizki.ecommerce.dto.order.OrderItemResponse;
import id.web.fitrarizki.ecommerce.dto.order.OrderResponse;
import id.web.fitrarizki.ecommerce.model.Order;
import id.web.fitrarizki.ecommerce.model.OrderStatus;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface OrderService {
    OrderResponse checkout(CheckOutRequest checkOutRequest);

    Order getOrderById(Long id);

    List<Order> getOrdersByUserId(Long userId);
    PaginatedResponse<OrderResponse> getPaginatedOrdersByUserId(Long userId, Pageable pageable);

    List<Order> getOrdersByStatus(OrderStatus status);

    void cancelOrder(Long id);

    List<OrderItemResponse> getOrderItemsByOrderId(Long orderId);

    void updateOrderStatus(Long id, OrderStatus status);

    BigDecimal calculateOrderTotal(Long orderId);
}