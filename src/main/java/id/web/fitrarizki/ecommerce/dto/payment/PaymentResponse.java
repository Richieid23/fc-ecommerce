package id.web.fitrarizki.ecommerce.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private String xenditInvoiceId;
    private String xenditExternalId;
    private BigDecimal amount;
    private String xenditInvoiceStatus;
    private String xenditPaymentUrl;
    private String xenditPaymentMethod;
}
