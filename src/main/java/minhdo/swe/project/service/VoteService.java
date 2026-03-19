package minhdo.swe.project.service;

import lombok.RequiredArgsConstructor;
import minhdo.swe.project.dto.request.VoteRequest;
import minhdo.swe.project.dto.response.PostResponse;
import minhdo.swe.project.entity.Post;
import minhdo.swe.project.entity.PostVote;
import minhdo.swe.project.entity.User;
import minhdo.swe.project.entity.VoteType;
import minhdo.swe.project.exception.ResourceNotFoundException;
import minhdo.swe.project.mapper.PostMapper;
import minhdo.swe.project.repository.PostRepository;
import minhdo.swe.project.repository.PostVoteRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VoteService {

    private final PostRepository postRepository;
    private final PostVoteRepository postVoteRepository;
    private final PostMapper postMapper;

    @CacheEvict(value = "posts", key = "#postId")
    @Transactional
    public PostResponse vote(Long postId, User user, VoteRequest request) {
        VoteType newVote = request.getVoteType();
        Post post = postRepository.findById(postId)
                .filter(p -> !p.getIsDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", String.valueOf(postId)));

        var existing = postVoteRepository.findByPostAndUser(post, user);
        if (existing.isPresent()) {
            PostVote vote = existing.get();
            VoteType oldVote = vote.getVoteType();
            if (oldVote.equals(newVote)) {
                postVoteRepository.delete(vote);
                post.setScore(post.getScore() - oldVote.getValue());
            } else {
                vote.setVoteType(newVote);
                postVoteRepository.save(vote);
                post.setScore(post.getScore() - oldVote.getValue() + newVote.getValue());
            }
        } else {
            postVoteRepository.save(PostVote.builder().post(post).user(user).voteType(newVote).build());
            post.setScore(post.getScore() + newVote.getValue());
        }
        postRepository.save(post);
        return postMapper.toPostResponse(post);
    }

    @CacheEvict(value = "posts", key = "#postId")
    @Transactional
    public void removeVote(Long postId, User user) {
        Post post = postRepository.findById(postId)
                .filter(p -> !p.getIsDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", String.valueOf(postId)));

        PostVote vote = postVoteRepository.findByPostAndUser(post, user)
                .orElseThrow(() -> new IllegalArgumentException("You have not voted on this post"));
        post.setScore(post.getScore() - vote.getVoteType().getValue());
        postVoteRepository.delete(vote);
        postRepository.save(post);
    }
}
