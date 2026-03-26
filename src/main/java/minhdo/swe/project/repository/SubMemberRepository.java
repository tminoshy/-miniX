package minhdo.swe.project.repository;

import minhdo.swe.project.entity.Sub;
import minhdo.swe.project.entity.SubMembership;
import minhdo.swe.project.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubMemberRepository extends JpaRepository<SubMembership, Long> {
    long countBySub(Sub sub);

    boolean existsByUserAndSub(User user, Sub sub);

    boolean existsByUserAndSubAndRole(User user, Sub sub, SubMembership.Role role);

    Optional<SubMembership> findByUserAndSub(User user, Sub sub);

    @EntityGraph(attributePaths = {"user"})
    Page<SubMembership> findAllBySub(Sub sub, Pageable pageable);
}
