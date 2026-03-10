package minhdo.swe.project.repository;

import minhdo.swe.project.entity.Sub;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubRepository extends JpaRepository<Sub, Long> {
    Optional<Sub> findByName(String name);

    boolean existsByName(String name);
}
