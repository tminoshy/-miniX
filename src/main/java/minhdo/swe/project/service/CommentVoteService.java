package minhdo.swe.project.service;

import lombok.RequiredArgsConstructor;
import minhdo.swe.project.dto.request.VoteRequest;
import minhdo.swe.project.dto.response.CommentResponse;
import minhdo.swe.project.entity.Comment;
import minhdo.swe.project.entity.CommentVote;
import minhdo.swe.project.entity.Sub;
import minhdo.swe.project.entity.User;
import minhdo.swe.project.entity.VoteType;
import minhdo.swe.project.exception.NotAllowedException;
import minhdo.swe.project.exception.ResourceNotFoundException;
import minhdo.swe.project.mapper.CommentMapper;
import minhdo.swe.project.repository.CommentRepository;
import minhdo.swe.project.repository.CommentVoteRepository;
import minhdo.swe.project.repository.SubMemberRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentVoteService {

    private final CommentRepository commentRepository;
    private final CommentVoteRepository commentVoteRepository;
    private final CommentMapper commentMapper;
    private final SubMemberRepository subMemberRepository;

    @CacheEvict(value = "comments", key = "#commentId")
    @Transactional
    public CommentResponse vote(Long commentId, User user, VoteRequest request) {
        VoteType newVote = request.getVoteType();
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", String.valueOf(commentId)));

        Sub sub = comment.getPost().getSub();

        if (!subMemberRepository.existsByUserAndSub(user, sub)) {
            throw new NotAllowedException("You must join sub before voting");
        }

        var existing = commentVoteRepository.findByCommentAndUser(comment, user);
        if (existing.isPresent()) {
            CommentVote vote = existing.get();
            VoteType oldVote = vote.getVoteType();
            if (oldVote.equals(newVote)) {
                commentVoteRepository.delete(vote);
                comment.setScore(comment.getScore() - oldVote.getValue());
            } else {
                vote.setVoteType(newVote);
                commentVoteRepository.save(vote);
                comment.setScore(comment.getScore() - oldVote.getValue() + newVote.getValue());
            }
        } else {
            commentVoteRepository.save(CommentVote.builder().comment(comment).user(user).voteType(newVote).build());
            comment.setScore(comment.getScore() + newVote.getValue());
        }
        commentRepository.save(comment);
        return commentMapper.toCommentResponse(comment);
    }

    @CacheEvict(value = "comments", key = "#commentId")
    @Transactional
    public void removeVote(Long commentId, User user) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", String.valueOf(commentId)));

        CommentVote vote = commentVoteRepository.findByCommentAndUser(comment, user)
                .orElseThrow(() -> new IllegalArgumentException("You have not voted on this comment"));
        comment.setScore(comment.getScore() - vote.getVoteType().getValue());
        commentVoteRepository.delete(vote);
        commentRepository.save(comment);
    }
}
