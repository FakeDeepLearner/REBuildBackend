package com.rebuild.backend.model.entities.forum_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.entities.util_entitites.Auditable;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "posts")
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
@Data
public class ForumPost extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NonNull
    private String title;

    @NonNull
    private String content;

    @OneToMany(mappedBy = "associatedPost", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostResume> resumes;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @JsonIgnore
    private User user;

    @OneToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REMOVE},
            mappedBy = "associatedPost")
    @JsonIgnore
    private List<Comment> comments = new ArrayList<>();

    @Column(name = "comments_count", nullable = false)
    private int commentCount = 0;

    @Column(name = "is_edited")
    private boolean isEdited = false;

}
