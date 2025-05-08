package id.web.fitrarizki.ecommerce.repository;

import id.web.fitrarizki.ecommerce.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    @Query(value = """
    SELECT r.* FROM roles r
    JOIN users_roles ur ON ur.role_id = r.id
    JOIN users u ON u.id = ur.user_id
    WHERE u.id = :userId
    """, nativeQuery = true)
    List<Role> findByUserId(Long userId);

    Optional<Role> findByName(String name);
}
