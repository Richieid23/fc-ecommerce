package id.web.fitrarizki.ecommerce.service;

import id.web.fitrarizki.ecommerce.model.Order;

public interface EmailService {
    void notifySuccessfulPayment(Order order);
    void notifyUnsuccessfulPayment(Order order);
}
