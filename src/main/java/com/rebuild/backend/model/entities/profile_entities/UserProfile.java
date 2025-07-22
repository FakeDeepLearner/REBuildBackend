package com.rebuild.backend.model.entities.profile_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.model.entities.resume_entities.Education;
import com.rebuild.backend.model.entities.resume_entities.Experience;
import com.rebuild.backend.model.entities.resume_entities.Header;
import com.rebuild.backend.model.entities.resume_entities.ResumeSection;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.forms.resume_forms.SectionForm;
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

    @OneToMany(fetch = FetchType.EAGER, orphanRemoval = true, cascade = ALL)
    @JoinColumn(name = "parent_id", referencedColumnName = "id")
    private List<Experience> experienceList;

    @OneToMany(fetch = FetchType.EAGER, orphanRemoval = true, cascade = ALL)
    @JoinColumn(name = "parent_id", referencedColumnName = "id")
    private List<ResumeSection> sections;

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
                       List<ResumeSection> sections) {
        this.header = profileHeader;
        this.education = newEducation;
        this.experienceList = experiences;
        this.sections = sections;
    }


}
