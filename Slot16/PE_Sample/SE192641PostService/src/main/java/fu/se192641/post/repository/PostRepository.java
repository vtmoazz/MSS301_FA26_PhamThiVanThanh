package fu.se192641.post.repository;

import fu.se192641.post.entity.Post;
import fu.se192641.post.entity.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {

    /** Tham số null nghĩa là không lọc theo trường đó; advertiserName so khớp một phần, không phân biệt hoa thường. */
    @Query("""
            SELECT p FROM Post p
            WHERE (:advertiserName IS NULL OR LOWER(p.advertiserName) LIKE LOWER(CONCAT('%', :advertiserName, '%')))
              AND (:status IS NULL OR p.status = :status)
            """)
    Page<Post> search(@Param("advertiserName") String advertiserName,
                      @Param("status") PostStatus status,
                      Pageable pageable);
}
