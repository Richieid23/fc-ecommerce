package id.web.fitrarizki.ecommerce.service;

import id.web.fitrarizki.ecommerce.model.UserInfo;

public interface JwtService {
    String generateToken(UserInfo userInfo);
    boolean validateToken(String token);
    String getUsernameFromToken(String token);
}
