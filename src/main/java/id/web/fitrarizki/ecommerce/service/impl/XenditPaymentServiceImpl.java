package id.web.fitrarizki.ecommerce.service.impl;

import com.xendit.exception.XenditException;
import com.xendit.model.Invoice;
import id.web.fitrarizki.ecommerce.dto.payment.PaymentNotification;
import id.web.fitrarizki.ecommerce.dto.payment.PaymentResponse;
import id.web.fitrarizki.ecommerce.exception.ResourceNotFoundException;
import id.web.fitrarizki.ecommerce.model.Order;
import id.web.fitrarizki.ecommerce.model.OrderStatus;
import id.web.fitrarizki.ecommerce.model.User;
import id.web.fitrarizki.ecommerce.repository.OrderRepository;
import id.web.fitrarizki.ecommerce.repository.UserRepository;
import id.web.fitrarizki.ecommerce.service.EmailService;
import id.web.fitrarizki.ecommerce.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class XenditPaymentServiceImpl implements PaymentService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final EmailService emailService;

    @Override
    public PaymentResponse create(Order order) {
        User user = userRepository.findById(order.getUserId()).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Map<String, Object> params = new HashMap<>();
        params.put("external_id", order.getId().toString());
        params.put("amount", order.getTotalAmount().doubleValue());
        params.put("payer_email", user.getEmail());
        params.put("description", "Payment for order #"+order.getId().toString());

        try {
            Invoice invoice = Invoice.create(params);
            return PaymentResponse.builder()
                    .xenditPaymentUrl(invoice.getInvoiceUrl())
                    .xenditExternalId(invoice.getExternalId())
                    .xenditInvoiceId(invoice.getId())
                    .amount(order.getTotalAmount())
                    .xenditInvoiceStatus(invoice.getStatus())
                    .xenditPaymentMethod(invoice.getPaymentMethod())
                    .build();

        } catch (XenditException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public PaymentResponse getPayment(String paymentId) {
        try {
            Invoice invoice = Invoice.getById(paymentId);
            return PaymentResponse.builder()
                    .xenditPaymentUrl(invoice.getInvoiceUrl())
                    .xenditExternalId(invoice.getExternalId())
                    .xenditInvoiceId(invoice.getId())
                    .amount(BigDecimal.valueOf(invoice.getAmount().doubleValue()))
                    .xenditInvoiceStatus(invoice.getStatus())
                    .build();
        } catch (XenditException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean verifyPayment(String paymentId) {
        try {
            Invoice invoice = Invoice.getById(paymentId);
            return invoice.getStatus().equals("PAID");
        } catch (XenditException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void handleNotification(PaymentNotification notification) {
        String invoiceId = notification.getId();
        String status = notification.getStatus();

        Order order = orderRepository.findByXenditInvoiceId(invoiceId).orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        order.setXenditPaymentStatus(status);

        switch (status) {
            case "PAID":
                order.setStatus(OrderStatus.PAID);
                emailService.notifySuccessfulPayment(order);
                break;
            case "EXPIRED":
                order.setStatus(OrderStatus.CANCELLED);
                emailService.notifyUnsuccessfulPayment(order);
                break;
            case "FAILED":
                order.setStatus(OrderStatus.PAYMENT_FAILED);
                emailService.notifyUnsuccessfulPayment(order);
                break;
            case "PENDING":
                order.setStatus(OrderStatus.PENDING);
                emailService.notifyUnsuccessfulPayment(order);
                break;
            default:
        }

        if (notification.getPaymentMethod() != null) {
            order.setXenditPaymentMethod(notification.getPaymentMethod());
        }

        orderRepository.save(order);
    }
}
