package minhdo.swe.project.service;

import lombok.RequiredArgsConstructor;
import minhdo.swe.project.dto.request.CreatePostRequest;
import minhdo.swe.project.dto.request.UpdatePostRequest;
import minhdo.swe.project.dto.response.PostResponse;
import minhdo.swe.project.entity.Post;
import minhdo.swe.project.entity.Sub;
import minhdo.swe.project.entity.User;
import minhdo.swe.project.exception.ResourceNotFoundException;
import minhdo.swe.project.mapper.PostMapper;
import minhdo.swe.project.repository.PostRepository;
import minhdo.swe.project.repository.SubMemberRepository;
import minhdo.swe.project.repository.SubRepository;
import minhdo.swe.project.repository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final SubRepository subRepository;
    private final SubMemberRepository subMemberRepository;
    private final PostMapper postMapper;

    @Cacheable(value = "posts", key = "#id")
    @Transactional(readOnly = true)
    public PostResponse getPostById(Long id) {
        return postRepository.findById(id)
                .filter(post -> !post.getIsDeleted())
                .map(postMapper::toPostResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", String.valueOf(id)));
    }

    @CacheEvict(value = "posts", key = "#id")
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

    @CacheEvict(value = "posts", key = "#id")
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

    @Transactional
    public PostResponse createPost(User currentUser, String subName, CreatePostRequest request) {
        Sub sub = subRepository.findByName(subName)
                .orElseThrow(() -> new ResourceNotFoundException("Sub", "name", subName));

        if (!subMemberRepository.existsByUserAndSub(currentUser, sub)) {
            throw new IllegalArgumentException("user is not in sub");
        }

        Post post = postMapper.toEntity(request);
        post.setUser(currentUser);
        post.setSub(sub);

        return postMapper.toPostResponse(postRepository.save(post));
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> getPostsBySub(String subName, Pageable pageable) {
        Sub sub = subRepository.findByName(subName)
                .orElseThrow(() -> new ResourceNotFoundException("Sub", "subName", subName));
        return postRepository.findBySubOrderByCreatedAtDesc(sub, pageable)
                .map(postMapper::toPostResponse);
    }
}
