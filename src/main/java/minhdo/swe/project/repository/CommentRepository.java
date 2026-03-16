package minhdo.swe.project.repository;

import minhdo.swe.project.entity.Comment;
import minhdo.swe.project.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByPostAndIsDeletedFalseOrderByCreatedAtDesc(Post post, Pageable pageable);
    Optional<Comment> findByIdAndPostAndIsDeletedFalse(Long id, Post post);
}
