package com.rebuild.backend.model.entities.forum_entities;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.model.entities.resume_entities.PostResume;
import com.rebuild.backend.model.entities.users.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.search.engine.backend.types.Searchable;
import org.hibernate.search.engine.backend.types.Sortable;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;

import java.time.Instant;
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
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false, columnDefinition = "uuid")
    @GenericField(searchable = Searchable.YES)
    private UUID id;

    @NonNull
    @FullTextField(searchable = Searchable.YES)
    private String title;

    @NonNull
    @FullTextField(searchable = Searchable.YES)
    private String content;

    @Column(name = "author_name")
    private String authorUsername;

    @OneToMany(mappedBy = "associatedPost", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostResume> resumes;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JsonIgnore
    private User creatingUser;

    @GenericField(sortable = Sortable.YES, searchable = Searchable.YES)
    private Instant creationDate = Instant.now();

    @GenericField(sortable = Sortable.YES, searchable = Searchable.YES)
    private Instant lastModificationDate = Instant.now();

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
