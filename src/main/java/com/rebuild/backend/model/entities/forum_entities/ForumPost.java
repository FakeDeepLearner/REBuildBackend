package com.rebuild.backend.model.entities.forum_entities;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.model.entities.resume_entities.PostResume;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.utils.GenerateV7UUID;
import com.rebuild.backend.utils.converters.database_converters.LocalDateTimeDatabaseConverter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "posts")
@NamedQueries(
        value = {
                @NamedQuery(name = "ForumPost.countByIdAndUserId",
                        query = "SELECT COUNT(*) FROM ForumPost p WHERE p.id=?1 and p.creatingUser.id=?2")
        }
)
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
@Data
@Indexed
public class ForumPost {

    @Id
    @GenerateV7UUID
    @Column(nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @NonNull
    @FullTextField
    private String title;

    @NonNull
    @FullTextField
    private String content;

    @Column(name = "author_name")
    private String authorUsername;

    @OneToMany(mappedBy = "associatedPost", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostResume> resumes;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JsonIgnore
    private User creatingUser;

    @Convert(converter = LocalDateTimeDatabaseConverter.class)
    @JsonIgnore
    @GenericField
    private LocalDateTime creationDate = LocalDateTime.now();

    @Convert(converter = LocalDateTimeDatabaseConverter.class)
    @JsonIgnore
    @GenericField
    private LocalDateTime lastModificationDate = LocalDateTime.now();

    @OneToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REMOVE},
            mappedBy = "associatedPost", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Comment> comments = new ArrayList<>();

    @Column(name = "likes_count", nullable = false)
    private int likeCount = 0;

    @Column(name = "comments_count", nullable = false)
    private int commentCount = 0;


    @JsonGetter(value = "authorUsername")
    private String determineAuthorName()
    {
        return authorUsername != null ? authorUsername : "Anonymous";
    }

}
