package minhdo.swe.project.service;

import minhdo.swe.project.dto.response.CommentResponse;
import minhdo.swe.project.dto.response.PostResponse;
import minhdo.swe.project.dto.response.SavedItemResponse;
import minhdo.swe.project.entity.*;
import minhdo.swe.project.exception.ResourceNotFoundException;
import minhdo.swe.project.mapper.CommentMapper;
import minhdo.swe.project.mapper.PostMapper;
import minhdo.swe.project.repository.CommentRepository;
import minhdo.swe.project.repository.PostRepository;
import minhdo.swe.project.repository.SavedItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SavedItemServiceTest {

    @Mock private SavedItemRepository savedItemRepository;
    @Mock private PostRepository postRepository;
    @Mock private CommentRepository commentRepository;
    @Mock private PostMapper postMapper;
    @Mock private CommentMapper commentMapper;

    @InjectMocks private SavedItemService savedItemService;

    private User user;
    private Post post;
    private Comment comment;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("testuser").build();

        post = Post.builder()
                .id(1L)
                .title("Test Post")
                .body("Post Body")
                .user(user)
                .isDeleted(false)
                .build();

        comment = Comment.builder()
                .id(1L)
                .body("Test Comment")
                .user(user)
                .post(post)
                .isDeleted(false)
                .build();
    }

    // --- savePost ---

    @Test
    void savePost_newSave_savesItem() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(savedItemRepository.findByUserAndPost(user, post)).thenReturn(Optional.empty());

        savedItemService.savePost(user, 1L);

        verify(savedItemRepository).save(argThat(item -> 
                item.getUser().equals(user) && 
                item.getPost().equals(post) && 
                item.getType() == SavedItemType.POST));
        verify(savedItemRepository, never()).delete(any());
    }

    @Test
    void savePost_alreadySaved_unsavesItem() {
        SavedItem existingItem = SavedItem.builder().id(10L).user(user).post(post).type(SavedItemType.POST).build();

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(savedItemRepository.findByUserAndPost(user, post)).thenReturn(Optional.of(existingItem));

        savedItemService.savePost(user, 1L);

        verify(savedItemRepository).delete(existingItem);
        verify(savedItemRepository, never()).save(any());
    }

    @Test
    void savePost_postNotFound_throwsException() {
        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> savedItemService.savePost(user, 99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Post not found with id:99");
    }

    @Test
    void savePost_postDeleted_throwsException() {
        post.setIsDeleted(true);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        assertThatThrownBy(() -> savedItemService.savePost(user, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Post not found with id:1");
    }

    // --- saveComment ---

    @Test
    void saveComment_newSave_savesItem() {
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(savedItemRepository.findByUserAndComment(user, comment)).thenReturn(Optional.empty());

        savedItemService.saveComment(user, 1L);

        verify(savedItemRepository).save(argThat(item -> 
                item.getUser().equals(user) && 
                item.getComment().equals(comment) && 
                item.getType() == SavedItemType.COMMENT));
        verify(savedItemRepository, never()).delete(any());
    }

    @Test
    void saveComment_alreadySaved_unsavesItem() {
        SavedItem existingItem = SavedItem.builder().id(11L).user(user).comment(comment).type(SavedItemType.COMMENT).build();

        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(savedItemRepository.findByUserAndComment(user, comment)).thenReturn(Optional.of(existingItem));

        savedItemService.saveComment(user, 1L);

        verify(savedItemRepository).delete(existingItem);
        verify(savedItemRepository, never()).save(any());
    }

    @Test
    void saveComment_commentNotFound_throwsException() {
        when(commentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> savedItemService.saveComment(user, 99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Comment not found with id:99");
    }

    @Test
    void saveComment_commentDeleted_throwsException() {
        comment.setIsDeleted(true);
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        assertThatThrownBy(() -> savedItemService.saveComment(user, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Comment not found with id:1");
    }

    // --- unsavePost ---

    @Test
    void unsavePost_success() {
        SavedItem existingItem = SavedItem.builder().id(10L).user(user).post(post).type(SavedItemType.POST).build();

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(savedItemRepository.findByUserAndPost(user, post)).thenReturn(Optional.of(existingItem));

        savedItemService.unsavePost(user, 1L);

        verify(savedItemRepository).delete(existingItem);
    }

    @Test
    void unsavePost_notSaved_doesNothing() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(savedItemRepository.findByUserAndPost(user, post)).thenReturn(Optional.empty());

        savedItemService.unsavePost(user, 1L);

        verify(savedItemRepository, never()).delete(any());
    }

    // --- unsaveComment ---

    @Test
    void unsaveComment_success() {
        SavedItem existingItem = SavedItem.builder().id(11L).user(user).comment(comment).type(SavedItemType.COMMENT).build();

        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(savedItemRepository.findByUserAndComment(user, comment)).thenReturn(Optional.of(existingItem));

        savedItemService.unsaveComment(user, 1L);

        verify(savedItemRepository).delete(existingItem);
    }

    @Test
    void unsaveComment_notSaved_doesNothing() {
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(savedItemRepository.findByUserAndComment(user, comment)).thenReturn(Optional.empty());

        savedItemService.unsaveComment(user, 1L);

        verify(savedItemRepository, never()).delete(any());
    }

    // --- getSavedItems ---

    @Test
    void getSavedItems_withoutType_returnsAllItems() {
        Pageable pageable = PageRequest.of(0, 10);
        SavedItem item1 = SavedItem.builder().id(1L).type(SavedItemType.POST).post(post).createdAt(LocalDateTime.now()).build();
        SavedItem item2 = SavedItem.builder().id(2L).type(SavedItemType.COMMENT).comment(comment).createdAt(LocalDateTime.now()).build();
        Page<SavedItem> page = new PageImpl<>(List.of(item1, item2));

        when(savedItemRepository.findByUserOrderByCreatedAtDesc(user, pageable)).thenReturn(page);
        
        PostResponse pr = mock(PostResponse.class);
        CommentResponse cr = mock(CommentResponse.class);
        when(postMapper.toPostResponse(post)).thenReturn(pr);
        when(commentMapper.toCommentResponse(comment)).thenReturn(cr);

        Page<SavedItemResponse> result = savedItemService.getSavedItems(user, null, pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getType()).isEqualTo(SavedItemType.POST);
        assertThat(result.getContent().get(0).getPost()).isEqualTo(pr);
        assertThat(result.getContent().get(1).getType()).isEqualTo(SavedItemType.COMMENT);
        assertThat(result.getContent().get(1).getComment()).isEqualTo(cr);
    }

    @Test
    void getSavedItems_withType_returnsFilteredItems() {
        Pageable pageable = PageRequest.of(0, 10);
        SavedItem item1 = SavedItem.builder().id(1L).type(SavedItemType.POST).post(post).createdAt(LocalDateTime.now()).build();
        Page<SavedItem> page = new PageImpl<>(List.of(item1));

        when(savedItemRepository.findByUserAndTypeOrderByCreatedAtDesc(user, SavedItemType.POST, pageable)).thenReturn(page);
        
        PostResponse pr = mock(PostResponse.class);
        when(postMapper.toPostResponse(post)).thenReturn(pr);

        Page<SavedItemResponse> result = savedItemService.getSavedItems(user, SavedItemType.POST, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getType()).isEqualTo(SavedItemType.POST);
        verify(commentMapper, never()).toCommentResponse(any());
    }
}
