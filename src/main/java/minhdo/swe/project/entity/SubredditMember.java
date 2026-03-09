package minhdo.swe.project.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "sub_members", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "user_id", "subreddit_id" })
})
@EntityListeners(AuditingEntityListener.class)
public class SubredditMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subreddit_id", nullable = false)
    private Subreddit subreddit;

    @Column(nullable = false, length = 20)
    private String role = "member";

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
