package minhdo.swe.project.service;

import lombok.RequiredArgsConstructor;
import minhdo.swe.project.entity.Sub;
import minhdo.swe.project.entity.SubMembership;
import minhdo.swe.project.entity.User;
import minhdo.swe.project.repository.SubMemberRepository;
import minhdo.swe.project.repository.SubRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubMembershipService {

    private final SubRepository subRepository;
    private final SubMemberRepository subMemberRepository;

    @Transactional
    public void join(String name, User currentUser) {
        Sub sub = subRepository.findByName(name)
                .orElseThrow(() -> new UsernameNotFoundException("Sub not found"));

        if (subMemberRepository.existsByUserAndSub(currentUser, sub)) {
            throw new IllegalArgumentException("Already a member");
        }

        SubMembership member = new SubMembership();
        member.setUser(currentUser);
        member.setSub(sub);
        member.setRole(SubMembership.Role.Member);
        subMemberRepository.save(member);
    }

    @Transactional
    public void leave(String name, User currentUser) {
        Sub sub = subRepository.findByName(name)
                .orElseThrow(() -> new UsernameNotFoundException("Sub not found"));

        SubMembership member = subMemberRepository.findByUserAndSub(currentUser, sub)
                .orElseThrow(() -> new IllegalArgumentException("Not a member"));

        if (SubMembership.Role.Moderator.equals(member.getRole())) {
            throw new IllegalArgumentException("Moderators cannot leave");
        }

        subMemberRepository.delete(member);
    }
}
