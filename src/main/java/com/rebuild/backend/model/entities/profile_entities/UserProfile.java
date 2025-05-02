package com.rebuild.backend.model.entities.profile_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.model.entities.users.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

import static jakarta.persistence.CascadeType.*;


@Data
@RequiredArgsConstructor
//@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "profiles")
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JsonIgnore
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

    @Column(name = "page_size")
    private int forumPageSize = 20;

    @OneToOne(fetch = FetchType.LAZY, cascade = {
            PERSIST,
            MERGE,
            REFRESH
    })
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @JsonIgnore
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
