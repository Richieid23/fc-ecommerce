package id.web.fitrarizki.ecommerce.service.impl;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import id.web.fitrarizki.ecommerce.config.AppProp;
import id.web.fitrarizki.ecommerce.config.SendgridConfig;
import id.web.fitrarizki.ecommerce.exception.UserNotFoundException;
import id.web.fitrarizki.ecommerce.model.Order;
import id.web.fitrarizki.ecommerce.model.OrderStatus;
import id.web.fitrarizki.ecommerce.model.User;
import id.web.fitrarizki.ecommerce.repository.UserRepository;
import id.web.fitrarizki.ecommerce.service.EmailService;
import io.github.resilience4j.retry.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final SendgridConfig sendgridConfig;
    private final SendGrid sendGrid;
    private final UserRepository userRepository;
    private final AppProp appProp;
    private final Retry emailRetrier;

    @Async
    @Override
    public void notifySuccessfulPayment(Order order) {
        User user = userRepository.findById(order.getUserId()).orElseThrow(() -> new UserNotFoundException("User not found"));
        Mail mail = prepareSuccessfulPaymentEmail(user, order);
        sendEmailWithRetry(mail);
    }

    @Async
    @Override
    public void notifyUnsuccessfulPayment(Order order) {
        User user = userRepository.findById(order.getUserId()).orElseThrow(() -> new UserNotFoundException("User not found"));
        Mail mail = prepareUnsuccessfulPaymentEmail(user, order);
        sendEmailWithRetry(mail);
    }

    private Mail prepareSuccessfulPaymentEmail(User user, Order order) {
        Email from = new Email(appProp.getSendgrid().getFrom());
        Email to = new Email(user.getEmail());

        Mail mail = new Mail();
        mail.setFrom(from);
        mail.setReplyTo(from);
        mail.setTemplateId(appProp.getSendgrid().getTemplate().getPaymentSuccessfulId());

        Personalization personalization = new Personalization();
        personalization.addTo(to);
        personalization.addDynamicTemplateData("customerName", user.getUsername());
        personalization.addDynamicTemplateData("orderId", order.getId().toString());
        personalization.addDynamicTemplateData("amount", order.getTotalAmount().toString());
        personalization.addDynamicTemplateData("paymentDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm")));
        mail.addPersonalization(personalization);

        return mail;
    }

    private Mail prepareUnsuccessfulPaymentEmail(User user, Order order) {
        Email from = new Email(appProp.getSendgrid().getFrom());
        Email to = new Email(user.getEmail());

        Mail mail = new Mail();
        mail.setFrom(from);
        mail.setReplyTo(from);
        mail.setTemplateId(appProp.getSendgrid().getTemplate().getPaymentFailedId());

        Personalization personalization = new Personalization();
        personalization.addTo(to);
        personalization.addDynamicTemplateData("customerName", user.getUsername());
        personalization.addDynamicTemplateData("orderId", order.getId().toString());
        personalization.addDynamicTemplateData("amount", order.getTotalAmount().toString());
        personalization.addDynamicTemplateData("paymentDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm")));
        personalization.addDynamicTemplateData("failedReason", failedReasonMessage(order.getStatus()));
        mail.addPersonalization(personalization);

        return mail;
    }

    private String failedReasonMessage(OrderStatus status) {
        return switch (status) {
            case CANCELLED -> "Pembayaran telah kedaluwarsa. Silakan lakukan pemesanan ulang.";
            case PAYMENT_FAILED ->
                    "Pembayaran gagal diproses. Mohon periksa metode pembayaran Anda dan coba lagi.";
            case PENDING ->
                    "Pembayaran masih dalam proses. Mohon tunggu beberapa saat dan periksa kembali status pesanan Anda.";
            default ->
                    "Terjadi kesalahan dalam proses pembayaran. Silakan hubungi layanan pelanggan kami untuk bantuan lebih lanjut.";
        };
    }

    private void sendEmail(Mail mail) throws IOException {
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());

        Response response = sendGrid.api(request);

        if (response.getStatusCode() > 299) {
            log.error("Failed to send email with status code: {}, body : {}", response.getStatusCode(), response.getBody());
            throw new IOException("Failed to send email with status code: " + response.getStatusCode());
        }
    }

    private void sendEmailWithRetry(Mail mail) {
        try {
            emailRetrier.executeCallable(() -> {
                sendEmail(mail);
                return null;
            });
        } catch (Exception e) {
            log.error("Failed to send email: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
}
