package com.rebuild.backend.model.entities.profile_entities;

import com.rebuild.backend.model.entities.users.User;
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
        @NamedQuery(name = "UserProfile.deleteProfileHeaderById",
                query = "UPDATE UserProfile p SET p.header = null WHERE p.id = ?1"),
        @NamedQuery(name = "UserProfile.deleteProfileEducationById",
                query = "UPDATE UserProfile p SET p.education = null WHERE p.id = ?1"),
        @NamedQuery(name = "UserProfile.deleteProfileExperiencesById",
                query = "UPDATE UserProfile p SET p.experienceList = null WHERE p.id = ?1"),
        @NamedQuery(name = "UserProfile.deleteProfileSectionsById",
                query = "UPDATE UserProfile p SET p.sections = null WHERE p.id = ?1")


}
)
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.EAGER, orphanRemoval = true, cascade = {
            ALL
    }, mappedBy = "profile")
    private ProfileHeader header;

    @OneToOne(fetch = FetchType.EAGER, orphanRemoval = true, cascade = ALL, mappedBy = "profile")
    private ProfileEducation education;

    @OneToMany(fetch = FetchType.EAGER, orphanRemoval = true, cascade = ALL, mappedBy = "profile")
    private List<ProfileExperience> experienceList;

    @OneToMany(fetch = FetchType.EAGER, orphanRemoval = true, cascade = ALL, mappedBy = "profile")
    private List<ProfileSection> sections;

    @OneToOne(fetch = FetchType.LAZY, cascade = {
            PERSIST,
            MERGE,
            REFRESH
    })
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    public UserProfile(ProfileHeader profileHeader,
                       ProfileEducation newEducation,
                       List<ProfileExperience> experiences,
                       List<ProfileSection> sections) {
        this.header = profileHeader;
        this.education = newEducation;
        this.experienceList = experiences;
        this.sections = sections;
    }
}
