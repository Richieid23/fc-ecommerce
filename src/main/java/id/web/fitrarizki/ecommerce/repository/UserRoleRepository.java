package id.web.fitrarizki.ecommerce.repository;

import id.web.fitrarizki.ecommerce.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UserRole.UserRoleId> {
    void deleteByIdUserId(Long userId);
}
