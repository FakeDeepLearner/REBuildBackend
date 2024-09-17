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

    @OneToOne(fetch = FetchType.EAGER, orphanRemoval = true)
    private ProfileHeader header;

    @OneToOne(fetch = FetchType.EAGER, orphanRemoval = true)
    private ProfileEducation education;

    @OneToMany(fetch = FetchType.EAGER, orphanRemoval = true)
    private List<ProfileExperience> experienceList;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    public UserProfile(ProfileHeader profileHeader,
                       ProfileEducation newEducation,
                       List<ProfileExperience> experiences) {
        this.header = profileHeader;
        this.education = newEducation;
        this.experienceList = experiences;
    }
}
