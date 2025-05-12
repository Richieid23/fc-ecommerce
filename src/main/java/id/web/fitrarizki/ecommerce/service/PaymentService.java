package id.web.fitrarizki.ecommerce.service;

import id.web.fitrarizki.ecommerce.dto.payment.PaymentNotification;
import id.web.fitrarizki.ecommerce.dto.payment.PaymentResponse;
import id.web.fitrarizki.ecommerce.model.Order;

public interface PaymentService {
    PaymentResponse create(Order order);
    PaymentResponse getPayment(String paymentId);
    boolean verifyPayment(String paymentId);
    void handleNotification(PaymentNotification notification);
}
