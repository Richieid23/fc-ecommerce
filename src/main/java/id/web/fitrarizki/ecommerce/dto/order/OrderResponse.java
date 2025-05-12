package id.web.fitrarizki.ecommerce.dto.order;

import id.web.fitrarizki.ecommerce.model.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private Long orderId;
    private Long userId;
    private BigDecimal subtotal;
    private BigDecimal shippingFee;
    private BigDecimal taxFee;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime orderDate;
    private String xenditInvoiceId;
    private String xenditPaymentStatus;
    private String xenditPaymentMethod;
    private String paymentUrl;

    public static OrderResponse fromOrder(Order order) {
        return OrderResponse.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .subtotal(order.getSubtotal())
                .shippingFee(order.getShippingFee())
                .taxFee(order.getTaxFee())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .orderDate(order.getOrderDate())
                .xenditInvoiceId(order.getXenditInvoiceId())
                .xenditPaymentStatus(order.getXenditPaymentStatus())
                .xenditPaymentMethod(order.getXenditPaymentMethod())
                .build();
    }
}
