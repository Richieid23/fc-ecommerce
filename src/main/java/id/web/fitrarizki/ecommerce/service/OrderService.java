package id.web.fitrarizki.ecommerce.service;

import id.web.fitrarizki.ecommerce.dto.order.CheckOutRequest;
import id.web.fitrarizki.ecommerce.dto.order.OrderItemResponse;
import id.web.fitrarizki.ecommerce.dto.order.OrderResponse;
import id.web.fitrarizki.ecommerce.model.Order;

import java.math.BigDecimal;
import java.util.List;

public interface OrderService {
    OrderResponse checkout(CheckOutRequest checkOutRequest);

    Order getOrderById(Long id);

    List<Order> getOrdersByUserId(Long userId);

    List<Order> getOrdersByStatus(String status);

    void cancelOrder(Long id);

    List<OrderItemResponse> getOrderItemsByOrderId(Long orderId);

    void updateOrderStatus(Long id, String status);

    BigDecimal calculateOrderTotal(Long orderId);
}