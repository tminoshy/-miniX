package minhdo.swe.project.service;

import lombok.RequiredArgsConstructor;
import minhdo.swe.project.dto.request.*;
import minhdo.swe.project.dto.response.*;
import minhdo.swe.project.entity.Sub;
import minhdo.swe.project.entity.SubMember;
import minhdo.swe.project.entity.User;
import minhdo.swe.project.exception.ResourceNotFoundException;
import minhdo.swe.project.mapper.SubMapper;
import minhdo.swe.project.mapper.UserMapper;
import minhdo.swe.project.repository.SubMemberRepository;
import minhdo.swe.project.repository.SubRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@Service
@RequiredArgsConstructor
public class SubService {

    private final SubRepository subRepository;
    private final SubMemberRepository memberRepository;
    private final SubMapper subMapper;
    private final UserMapper userMapper;
    private final SubMemberRepository subMemberRepository;

    @Transactional
    public SubResponse create(User user, CreateSubRequest request) {
        if (subRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Sub name already taken: " + request.getName());
        }

        Sub sub = new Sub();
        sub.setName(request.getName());
        sub.setDescription(request.getDescription());
        sub.setIconUrl(request.getIconUrl());
        sub.setCreatedBy(user);
        sub = subRepository.save(sub);

        SubMember member = new SubMember();
        member.setUser(user);
        member.setSub(sub);
        member.setRole(SubMember.Role.Moderator);
        memberRepository.save(member);

        return subMapper.toSubResponse(sub, 1);
    }

    public SubDetailResponse getByName(String name, User currentUser) {
        Sub sub = subRepository.findByName(name)
                .orElseThrow(() -> new UsernameNotFoundException("Sub not found"));

        long memberCount = memberRepository.countBySub(sub);

        boolean isMember = memberRepository.existsByUserAndSub(currentUser, sub);

        return subMapper.toDetailResponse(sub, memberCount, isMember);
    }

    @Transactional
    public SubDetailResponse update(String name, User currentUser, UpdateSubRequest request) {
        Sub sub = subRepository.findByName(name)
                .orElseThrow(() -> new UsernameNotFoundException("Sub not found"));

        boolean isModerator = memberRepository.existsByUserAndSubAndRole(currentUser, sub, SubMember.Role.Moderator);
        if (!isModerator) {
            throw new AccessDeniedException("Only moderators can update this sub");
        }

        if (request.getDescription() != null) {
            sub.setDescription(request.getDescription());
        }
        if (request.getIconUrl() != null) {
            sub.setIconUrl(request.getIconUrl());
        }
        sub = subRepository.save(sub);

        long memberCount = memberRepository.countBySub(sub);
        return subMapper.toDetailResponse(sub, memberCount, true);
    }

    @Transactional
    public void join(String name, User currentUser) {
        Sub sub = subRepository.findByName(name)
                .orElseThrow(() -> new UsernameNotFoundException("Sub not found"));

        if (memberRepository.existsByUserAndSub(currentUser, sub)) {
            throw new IllegalArgumentException("Already a member");
        }

        SubMember member = new SubMember();
        member.setUser(currentUser);
        member.setSub(sub);
        member.setRole(SubMember.Role.Member);
        memberRepository.save(member);
    }

    @Transactional
    public void leave(String name, User currentUser) {
        Sub sub = subRepository.findByName(name)
                .orElseThrow(() -> new UsernameNotFoundException("Sub not found"));

        SubMember member = memberRepository.findByUserAndSub(currentUser, sub)
                .orElseThrow(() -> new IllegalArgumentException("Not a member"));

        if (SubMember.Role.Moderator.equals(member.getRole())) {
            throw new IllegalArgumentException("Moderators cannot leave");
        }

        memberRepository.delete(member);
    }

    @Transactional(readOnly = true)
    public Page<UserProfileResponse> showAllMember(String subName, Pageable pageable) {
        Sub sub = subRepository.findByName(subName)
                .orElseThrow(() -> new ResourceNotFoundException("Sub", "subName", subName));

        return subMemberRepository.findAllBySub(sub, pageable)
                .map(subMember -> userMapper.toProfileResponse(subMember.getUser()));
    }
}
