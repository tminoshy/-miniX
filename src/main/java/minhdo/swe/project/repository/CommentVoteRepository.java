package minhdo.swe.project.repository;

import minhdo.swe.project.entity.Comment;
import minhdo.swe.project.entity.CommentVote;
import minhdo.swe.project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommentVoteRepository extends JpaRepository<CommentVote, Long> {
    Optional<CommentVote> findByCommentAndUser(Comment comment, User user);
}
