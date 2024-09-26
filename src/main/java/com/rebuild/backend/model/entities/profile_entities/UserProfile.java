package com.rebuild.backend.model.entities.profile_entities;

import com.rebuild.backend.model.entities.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

import static jakarta.persistence.CascadeType.*;


@Data
@RequiredArgsConstructor
// @NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "profiles")
@NamedQueries({
        @NamedQuery(name = "UserProfile.deleteProfileHeaderByUserId",
                query = "UPDATE UserProfile p SET p.header = null WHERE p.user.id = ?1"),
        @NamedQuery(name = "UserProfile.deleteProfileEducationByUserId",
                query = "UPDATE UserProfile p SET p.education = null WHERE p.user.id = ?1"),
        @NamedQuery(name = "UserProfile.deleteProfileExperiencesByUserId",
                query = "UPDATE UserProfile p SET p.experienceList = null WHERE p.user.id = ?1")

}
)
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.EAGER, orphanRemoval = true, cascade = {
            ALL
    })
    private ProfileHeader header;

    @OneToOne(fetch = FetchType.EAGER, orphanRemoval = true, cascade = ALL)
    private ProfileEducation education;

    @OneToMany(fetch = FetchType.EAGER, orphanRemoval = true, cascade = ALL)
    private List<ProfileExperience> experienceList;

    @OneToOne(fetch = FetchType.LAZY, cascade = {
            PERSIST,
            MERGE,
            REFRESH
    })
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
