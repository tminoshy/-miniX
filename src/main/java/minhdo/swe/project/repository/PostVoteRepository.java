package minhdo.swe.project.repository;

import minhdo.swe.project.entity.Post;
import minhdo.swe.project.entity.PostVote;
import minhdo.swe.project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostVoteRepository extends JpaRepository<PostVote, Long> {
    Optional<PostVote> findByPostAndUser(Post post, User user);
}
