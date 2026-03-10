package minhdo.swe.project.service;

import lombok.RequiredArgsConstructor;
import minhdo.swe.project.dto.request.CreatePostRequest;
import minhdo.swe.project.dto.response.PostResponse;
import minhdo.swe.project.entity.Post;
import minhdo.swe.project.entity.PostType;
import minhdo.swe.project.entity.Sub;
import minhdo.swe.project.entity.User;
import minhdo.swe.project.exception.ResourceNotFoundException;
import minhdo.swe.project.mapper.PostMapper;
import minhdo.swe.project.repository.PostRepository;
import minhdo.swe.project.repository.SubRepository;
import minhdo.swe.project.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final SubRepository subRepository;
    private final PostMapper postMapper;

    public PostResponse createPost(User currentUser, String subName, CreatePostRequest request) {
        Sub sub = subRepository.findByName(subName)
                .orElseThrow(() -> new ResourceNotFoundException("Sub", "name", subName));

        Post post = postMapper.toEntity(request);
        post.setUser(currentUser);
        post.setSub(sub);

        return postMapper.toPostResponse(postRepository.save(post));
    }

//    public PostResponse getPost(Long id) {
//        Post post = postRepository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("Post not found: " + id));
//        if (post.getIsDeleted()) {
//            throw new IllegalArgumentException("Post has been deleted");
//        }
//        return toPostResponse(post);
//    }
//
//    public List<PostResponse> getAllPosts() {
//        return postRepository.findByIsDeletedFalseOrderByCreatedAtDesc()
//                .stream()
//                .map(this::toPostResponse)
//                .toList();
//    }
//
//    public List<PostResponse> getPostsBySub(Long subId) {
//        return postRepository.findBySubIdOrderByCreatedAtDesc(subId)
//                .stream()
//                .filter(p -> !p.getIsDeleted())
//                .map(this::toPostResponse)
//                .toList();
//    }
//
//    public List<PostResponse> getPostsByUser(Long userId) {
//        return postRepository.findByUserIdOrderByCreatedAtDesc(userId)
//                .stream()
//                .filter(p -> !p.getIsDeleted())
//                .map(this::toPostResponse)
//                .toList();
//    }
//
//    public void deletePost(String username, Long postId) {
//        Post post = postRepository.findById(postId)
//                .orElseThrow(() -> new IllegalArgumentException("Post not found: " + postId));
//        if (!post.getUser().getUsername().equals(username)) {
//            throw new IllegalArgumentException("You can only delete your own posts");
//        }
//        post.setIsDeleted(true);
//        postRepository.save(post);
//    }
}
