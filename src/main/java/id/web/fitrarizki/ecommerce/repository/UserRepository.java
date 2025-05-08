package id.web.fitrarizki.ecommerce.repository;

import id.web.fitrarizki.ecommerce.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query(value = """
    SELECT * FROM users
    WHERE username = :keyword OR
    email = :keyword
    """, nativeQuery = true)
    Optional<User> findByKeyword(String keyword);

    @Query(value = """
    SELECT * FROM users
    WHERE lower(username) LIKE lower(concat('%', :keyword, '%'))
    OR lower(email) LIKE lower(concat('%', :keyword, '%'))
    """, nativeQuery = true)
    Page<User> searchUser(String keyword, Pageable pageable);

    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
}
