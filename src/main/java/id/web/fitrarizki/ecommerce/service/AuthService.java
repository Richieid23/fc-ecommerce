package id.web.fitrarizki.ecommerce.service;

import id.web.fitrarizki.ecommerce.dto.user.AuthRequest;
import id.web.fitrarizki.ecommerce.model.UserInfo;

public interface AuthService {
    UserInfo authenticate(AuthRequest authRequest);
}
