package minhdo.swe.project.service;

import minhdo.swe.project.dto.CreatePostRequest;
import minhdo.swe.project.dto.PostResponse;
import minhdo.swe.project.entity.Post;
import minhdo.swe.project.entity.PostType;
import minhdo.swe.project.entity.User;
import minhdo.swe.project.repository.PostRepository;
import minhdo.swe.project.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public PostService(PostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    public PostResponse createPost(String username, CreatePostRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        PostType postType = PostType.valueOf(request.getType().toUpperCase());

        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setType(postType);
        post.setBody(request.getBody());
        post.setUrl(request.getUrl());
        post.setImageUrl(request.getImageUrl());
        post.setUser(user);
        post.setSubId(request.getSubId());

        post = postRepository.save(post);
        return toPostResponse(post);
    }

    public PostResponse getPost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post not found: " + id));
        if (post.getIsDeleted()) {
            throw new IllegalArgumentException("Post has been deleted");
        }
        return toPostResponse(post);
    }

    public List<PostResponse> getAllPosts() {
        return postRepository.findByIsDeletedFalseOrderByCreatedAtDesc()
                .stream()
                .map(this::toPostResponse)
                .toList();
    }

    public List<PostResponse> getPostsBySub(Long subId) {
        return postRepository.findBySubIdOrderByCreatedAtDesc(subId)
                .stream()
                .filter(p -> !p.getIsDeleted())
                .map(this::toPostResponse)
                .toList();
    }

    public List<PostResponse> getPostsByUser(Long userId) {
        return postRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .filter(p -> !p.getIsDeleted())
                .map(this::toPostResponse)
                .toList();
    }

    public void deletePost(String username, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found: " + postId));
        if (!post.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("You can only delete your own posts");
        }
        post.setIsDeleted(true);
        postRepository.save(post);
    }

    private PostResponse toPostResponse(Post post) {
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getType().name(),
                post.getBody(),
                post.getUrl(),
                post.getImageUrl(),
                post.getUser().getId(),
                post.getUser().getUsername(),
                post.getSubId(),
                post.getScore(),
                post.getCreatedAt());
    }
}
