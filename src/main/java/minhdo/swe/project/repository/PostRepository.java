package minhdo.swe.project.repository;

import minhdo.swe.project.entity.Post;
import minhdo.swe.project.entity.Sub;
import minhdo.swe.project.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findByUserAndIsDeletedFalseOrderByCreatedAtDesc(User user, Pageable pageable);

    Page<Post> findBySubOrderByCreatedAtDesc(Sub sub, Pageable pageable);

    Page<Post> findByIsDeletedFalseOrderByCreatedAtDesc(Pageable pageable);
}
