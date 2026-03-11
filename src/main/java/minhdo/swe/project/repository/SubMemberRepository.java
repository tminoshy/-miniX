package minhdo.swe.project.repository;

import minhdo.swe.project.entity.Sub;
import minhdo.swe.project.entity.SubMember;
import minhdo.swe.project.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubMemberRepository extends JpaRepository<SubMember, Long> {
    long countBySub(Sub sub);

    boolean existsByUserAndSub(User user, Sub sub);

    boolean existsByUserAndSubAndRole(User user, Sub sub, SubMember.Role role);

    Optional<SubMember> findByUserAndSub(User user, Sub sub);

    Page<SubMember> findAllBySub(Sub sub, Pageable pageable);
}
