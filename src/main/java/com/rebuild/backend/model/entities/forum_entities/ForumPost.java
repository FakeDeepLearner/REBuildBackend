package com.rebuild.backend.model.entities.forum_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.utils.converters.database_converters.LocalDateTimeDatabaseConverter;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

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
@EntityListeners(AuditingEntityListener.class)
public class ForumPost {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JsonIgnore
    private UUID id;

    @NonNull
    private String title;

    @NonNull
    private String content;

    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    private Resume resume;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    private User creatingUser;

    @CreatedDate
    @Convert(converter = LocalDateTimeDatabaseConverter.class)
    private LocalDateTime creationDate = LocalDateTime.now();

    @LastModifiedDate
    @Convert(converter = LocalDateTimeDatabaseConverter.class)
    private LocalDateTime lastModificationDate = LocalDateTime.now();

    @OneToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REMOVE},
            mappedBy = "associatedPost")
    private List<Comment> comments = new ArrayList<>();

}
