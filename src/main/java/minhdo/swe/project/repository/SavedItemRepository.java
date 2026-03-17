package minhdo.swe.project.repository;

import minhdo.swe.project.entity.Comment;
import minhdo.swe.project.entity.Post;
import minhdo.swe.project.entity.SavedItem;
import minhdo.swe.project.entity.SavedItemType;
import minhdo.swe.project.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SavedItemRepository extends JpaRepository<SavedItem, Long> {

    Page<SavedItem> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    Page<SavedItem> findByUserAndTypeOrderByCreatedAtDesc(User user, SavedItemType type, Pageable pageable);

    Optional<SavedItem> findByUserAndPost(User user, Post post);

    Optional<SavedItem> findByUserAndComment(User user, Comment comment);
}
