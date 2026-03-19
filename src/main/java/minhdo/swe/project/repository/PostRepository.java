package minhdo.swe.project.repository;

import minhdo.swe.project.entity.Post;
import minhdo.swe.project.entity.Sub;
import minhdo.swe.project.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    @EntityGraph(attributePaths = {"user", "sub"})
    Page<Post> findByUserAndIsDeletedFalseOrderByCreatedAtDesc(User user, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "sub"})
    Page<Post> findBySubOrderByCreatedAtDesc(Sub sub, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "sub"})
    Page<Post> findByIsDeletedFalseOrderByCreatedAtDesc(Pageable pageable);
}
