package com.rebuild.backend.model.entities.profile_entities;

import com.rebuild.backend.model.entities.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;


@Data
@RequiredArgsConstructor
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "profiles")
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NonNull
    @OneToOne(fetch = FetchType.EAGER, orphanRemoval = true)
    private ProfileHeader header;

    @NonNull
    @OneToOne(fetch = FetchType.EAGER, orphanRemoval = true)
    private ProfileEducation education;

    @NonNull
    @OneToMany(fetch = FetchType.EAGER, orphanRemoval = true)
    private List<ProfileExperience> experienceList;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

}
