package minhdo.swe.project.service;

import lombok.RequiredArgsConstructor;
import minhdo.swe.project.dto.*;
import minhdo.swe.project.entity.Subreddit;
import minhdo.swe.project.entity.SubredditMember;
import minhdo.swe.project.entity.User;
import minhdo.swe.project.mapper.SubredditMapper;
import minhdo.swe.project.repository.SubredditMemberRepository;
import minhdo.swe.project.repository.SubredditRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubredditService {

    private final SubredditRepository subredditRepository;
    private final SubredditMemberRepository memberRepository;
    private final SubredditMapper subredditMapper;

    @Transactional
    public SubredditResponse create(User user, CreateSubredditRequest request) {
        if (subredditRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Subreddit name already taken: " + request.getName());
        }

        Subreddit subreddit = new Subreddit();
        subreddit.setName(request.getName());
        subreddit.setDescription(request.getDescription());
        subreddit.setIconUrl(request.getIconUrl());
        subreddit.setCreatedBy(user);
        subreddit = subredditRepository.save(subreddit);

        SubredditMember member = new SubredditMember();
        member.setUser(user);
        member.setSubreddit(subreddit);
        member.setRole("moderator");
        memberRepository.save(member);

        return subredditMapper.toSubredditResponse(subreddit, 1);
    }

    public SubredditDetailResponse getByName(String name, User currentUser) {
        Subreddit subreddit = subredditRepository.findByName(name)
                .orElseThrow(() -> new UsernameNotFoundException("Subreddit not found"));

        long memberCount = memberRepository.countBySubreddit(subreddit);

        boolean isMember = false;
        if (currentUser != null) {
            isMember = memberRepository.existsByUserAndSubreddit(currentUser, subreddit);
        }

        return subredditMapper.toDetailResponse(subreddit, memberCount, isMember);
    }

    @Transactional
    public SubredditDetailResponse update(String name, User currentUser, UpdateSubredditRequest request) {
        Subreddit subreddit = subredditRepository.findByName(name)
                .orElseThrow(() -> new UsernameNotFoundException("Subreddit not found"));

        boolean isModerator = memberRepository.existsByUserAndSubredditAndRole(currentUser, subreddit, "moderator");
        if (!isModerator) {
            throw new AccessDeniedException("Only moderators can update this subreddit");
        }

        if (request.getDescription() != null) {
            subreddit.setDescription(request.getDescription());
        }
        if (request.getIconUrl() != null) {
            subreddit.setIconUrl(request.getIconUrl());
        }
        subreddit = subredditRepository.save(subreddit);

        long memberCount = memberRepository.countBySubreddit(subreddit);
        return subredditMapper.toDetailResponse(subreddit, memberCount, true);
    }
}
