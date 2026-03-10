package minhdo.swe.project.repository;

import minhdo.swe.project.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Post> findBySubIdOrderByCreatedAtDesc(Long subId);

    List<Post> findByIsDeletedFalseOrderByCreatedAtDesc();
}
