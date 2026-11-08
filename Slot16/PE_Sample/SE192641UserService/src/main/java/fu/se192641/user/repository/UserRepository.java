package fu.se192641.user.repository;

import fu.se192641.user.entity.Role;
import fu.se192641.user.entity.User;
import fu.se192641.user.entity.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUsername(String username);

    boolean existsByUsernameAndUserIdNot(String username, Long userId);

    /** Bộ lọc tuỳ chọn: tham số null nghĩa là không lọc theo trường đó. */
    @Query("SELECT u FROM User u WHERE (:role IS NULL OR u.role = :role) AND (:status IS NULL OR u.status = :status)")
    Page<User> search(@Param("role") Role role, @Param("status") UserStatus status, Pageable pageable);
}
