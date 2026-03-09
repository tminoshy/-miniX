package minhdo.swe.project.repository;

import minhdo.swe.project.entity.Subreddit;
import minhdo.swe.project.entity.SubredditMember;
import minhdo.swe.project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubredditMemberRepository extends JpaRepository<SubredditMember, Long> {
    long countBySubreddit(Subreddit subreddit);

    boolean existsByUserAndSubreddit(User user, Subreddit subreddit);

    boolean existsByUserAndSubredditAndRole(User user, Subreddit subreddit, String role);
}
