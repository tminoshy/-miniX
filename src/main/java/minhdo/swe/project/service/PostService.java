package minhdo.swe.project.service;

import lombok.RequiredArgsConstructor;
import minhdo.swe.project.entity.User;
import minhdo.swe.project.mapper.PostMapper;
import minhdo.swe.project.repository.PostRepository;
import minhdo.swe.project.repository.UserRepository;
import minhdo.swe.project.dto.request.UpdatePostRequest;
import minhdo.swe.project.dto.response.PostResponse;
import minhdo.swe.project.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostMapper postMapper;

    @Transactional(readOnly = true)
    public PostResponse getPostById(Long id) {
        return postRepository.findById(id)
                .filter(post -> !post.getIsDeleted())
                .map(postMapper::toPostResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", String.valueOf(id)));
    }

    @Transactional
    public PostResponse updatePost(Long id, User user, UpdatePostRequest request) {
        var post = postRepository.findById(id)
                .filter(p -> !p.getIsDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", String.valueOf(id)));

        if (!user.equals(post.getUser())) {
            throw new IllegalArgumentException("you must be owner to modify the post");
        }

        post.setTitle(request.getTitle());
        post.setBody(request.getBody());

        return postMapper.toPostResponse(postRepository.save(post));
    }

    @Transactional
    public void deletePost(Long id, User user) {
        var post = postRepository.findById(id)
                .filter(p -> !p.getIsDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", String.valueOf(id)));
        if (!post.getUser().equals(user)) {
            throw new IllegalArgumentException("You can only delete your own posts");
        }
        post.setIsDeleted(true);
        postRepository.save(post);
    }


    public Page<PostResponse> getPostsByUser(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "Id", String.valueOf(userId)));
        return postRepository.findByUserAndIsDeletedFalseOrderByCreatedAtDesc(user, pageable)
                .map(postMapper::toPostResponse);
    }

}
