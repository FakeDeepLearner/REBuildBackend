package com.rebuild.backend.model.entities.profile_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.model.entities.resume_entities.*;
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
    })
    @JoinColumn(name = "header_id", referencedColumnName = "id")
    private Header header;

    @OneToOne(fetch = FetchType.EAGER, orphanRemoval = true, cascade = ALL)
    @JoinColumn(name = "education_id", referencedColumnName = "id")
    private Education education;

    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, cascade = ALL)
    @JoinColumn(name = "parent_id", referencedColumnName = "id")
    private List<Experience> experienceList;

    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, cascade = ALL)
    @JoinColumn(name = "parent_id", referencedColumnName = "id")
    private List<Section> sections;

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

    public UserProfile(Header profileHeader,
                       Education newEducation,
                       List<Experience> experiences,
                       List<Section> sections) {
        this.header = profileHeader;
        this.education = newEducation;
        this.experienceList = experiences;
        this.sections = sections;
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
                        oldExperience.getTechnologyList(), oldExperience.getLocation(), oldExperience.getStartDate(),
                        oldExperience.getEndDate(), oldExperience.getBullets())
        ).toList();

        List<Section> newSections = originalProfile.getSections().stream().map(
                oldSection -> {
                    List<SectionEntry> newEntries = oldSection.getEntries().stream().map(
                            oldEntry -> new SectionEntry(oldEntry.getTitle(),
                                    oldEntry.getToolsUsed(), oldEntry.getLocation(), oldEntry.getStartDate(),
                                    oldEntry.getEndDate(), oldEntry.getBullets())
                    ).toList();

                    return new Section(newEntries, oldSection.getTitle());
                }
        ).toList();

        return new UserProfile(newHeader, newEducation, newExperiences, newSections);
    }


}
