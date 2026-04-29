package com.rebuild.backend.model.entities.forum_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.model.entities.user_entities.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.search.engine.backend.types.Searchable;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "posts")
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
@Data
public class ForumPost {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    @GenericField(searchable = Searchable.YES)
    private UUID id;

    @NonNull
    private String title;

    @NonNull
    private String content;

    @OneToMany(mappedBy = "associatedPost", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostResume> resumes;

    @OneToMany(mappedBy = "associatedPost", cascade =  CascadeType.ALL, orphanRemoval = true)
    private List<ResumeFileUploadRecord> uploadedFiles;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @JsonIgnore
    private User user;

    private Instant creationDate = Instant.now();

    private Instant lastModificationDate = Instant.now();

    @OneToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REMOVE},
            mappedBy = "associatedPost")
    @JsonIgnore
    private List<Comment> comments = new ArrayList<>();

    @Column(name = "likes_count", nullable = false)
    private int likeCount = 0;

    @Column(name = "comments_count", nullable = false)
    private int commentCount = 0;

    private boolean isAnonymized;

}
