package minhdo.swe.project.repository;

import minhdo.swe.project.entity.Subreddit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubredditRepository extends JpaRepository<Subreddit, Long> {
    Optional<Subreddit> findByName(String name);

    boolean existsByName(String name);
}
