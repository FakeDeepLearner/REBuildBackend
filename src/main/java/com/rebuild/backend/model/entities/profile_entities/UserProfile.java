package com.rebuild.backend.model.entities.profile_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.model.entities.forum_entities.PostSearchConfiguration;
import com.rebuild.backend.model.entities.resume_entities.*;
import com.rebuild.backend.model.entities.users.User;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
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

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "profile")
    private Header header;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "profile")
    private Education education;

    @OneToMany(fetch = FetchType.LAZY, cascade = {
            CascadeType.ALL
    }, orphanRemoval = true, mappedBy = "resume")
    @OrderBy("endDate DESC NULLS FIRST, startDate DESC")
    private List<Experience> experienceList;

    @OneToMany(fetch = FetchType.LAZY, cascade = {
            CascadeType.ALL
    }, orphanRemoval = true, mappedBy = "resume")
    @OrderBy("endDate DESC NULLS FIRST, startDate DESC")
    private List<Project> projectList;

    @OneToOne(orphanRemoval = true, cascade = ALL, mappedBy = "associatedProfile")
    private ProfilePicture profilePicture;

    @OneToMany(cascade = ALL, mappedBy = "associatedProfile", orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ResumeSearchConfiguration> resumeSearchConfigurations = new ArrayList<>();

    @OneToMany(cascade = ALL, mappedBy = "associatedProfile", orphanRemoval = true, fetch = FetchType.EAGER)
    private List<PostSearchConfiguration> postSearchConfigurations = new ArrayList<>();

    @Column(name = "page_size")
    private int forumPageSize = 20;

    @Column(name = "exclusive_friend_messages")
    private boolean messagesFromFriendsOnly = false;
    
    @OneToOne(fetch = FetchType.LAZY, cascade = {
            PERSIST,
            MERGE,
            REFRESH
    })
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @JsonIgnore
    private User user;

    public UserProfile(Header profileHeader,
                       Education newEducation,
                       List<Experience> experiences) {
        this.header = profileHeader;
        this.education = newEducation;
        this.experienceList = experiences;
    }


    public static UserProfile deepCopy(UserProfile originalProfile){
        Header oldHeader = originalProfile.getHeader();
        Header newHeader = new Header(oldHeader.getNumber(), oldHeader.getFirstName(), oldHeader.getLastName(),
                oldHeader.getEmail());

        Education oldEducation = originalProfile.getEducation();

        Education newEducation = new Education(oldEducation.getSchoolName(),
                oldEducation.getRelevantCoursework(), oldEducation.getLocation(), oldEducation.getStartDate(),
                oldEducation.getEndDate());

        List<Experience> newExperiences = originalProfile.getExperienceList().stream().map(
                oldExperience -> new Experience(oldExperience.getCompanyName(),
                        oldExperience.getTechnologyList(), oldExperience.getLocation(),
                        oldExperience.getExperienceType(), oldExperience.getStartDate(),
                        oldExperience.getEndDate(), oldExperience.getBullets())
        ).toList();

        return new UserProfile(newHeader, newEducation, newExperiences);
    }


    public void addPostSearchConfig(PostSearchConfiguration configuration)
    {
        this.postSearchConfigurations.add(configuration);
    }

    public void addResumeSearchConfig(ResumeSearchConfiguration configuration)
    {
        this.resumeSearchConfigurations.add(configuration);
    }
}
