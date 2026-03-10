package com.rebuild.backend.model.entities.profile_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.model.entities.resume_entities.*;
import com.rebuild.backend.model.entities.user_entities.User;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import static jakarta.persistence.CascadeType.*;


@Data
@RequiredArgsConstructor
//@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "profiles", indexes = {
        @Index(columnList = "header_id"),
        @Index(columnList = "education_id"),
        @Index(columnList = "experience_id"),
        @Index(columnList = "user_id"),
        @Index(columnList = "picture_id")
})
public class UserProfile implements Serializable {

    @Serial
    private static final long serialVersionUID = 5L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @OneToOne(orphanRemoval = true, cascade = ALL, mappedBy = "associatedProfile")
    private ProfilePicture profilePicture;

    @OneToOne(mappedBy = "associatedProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private ProfileSettings settings;

    @OneToOne(fetch = FetchType.LAZY, cascade = {
            PERSIST,
            MERGE,
            REFRESH
    })
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @JsonIgnore
    private User user;

}
