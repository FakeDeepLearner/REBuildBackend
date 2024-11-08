package com.rebuild.backend.model.entities.resume_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.exceptions.resume_exceptions.MaxResumesReachedException;
import com.rebuild.backend.model.entities.users.User;
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
@Table(name = "resumes", uniqueConstraints = {
        @UniqueConstraint(name = "uk_same_user_resume_name", columnNames = {"user_id", "name"})
})
@NamedQueries(
        value = {
                @NamedQuery(name = "Resume.countByIdAndUserId",
                query = "SELECT COUNT(*) FROM Resume r WHERE r.id=?1 and r.user.id=?2")
        }
)
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Resume {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(
            name = "id"
    )
    private UUID id;

    @Column(name = "name", nullable = false)
    @NonNull
    private String name;

    @OneToOne(mappedBy = "resume", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Header header;

    @OneToOne(mappedBy = "resume", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Education education;

    @OneToMany(mappedBy = "resume", fetch = FetchType.EAGER, cascade = {
            CascadeType.ALL
    }, orphanRemoval = true)
    private List<Experience> experiences;

    @OneToMany(mappedBy = "resume", fetch = FetchType.EAGER,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResumeSection> sections;

    @ManyToOne(cascade = {
            CascadeType.REFRESH,
            CascadeType.PERSIST,
            CascadeType.MERGE
    })
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_user_id"))
    private User user;

    @OneToMany(mappedBy = "associatedResume", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResumeVersion> savedVersions;

    @JsonIgnore
    @CreatedDate
    private LocalDateTime creationTime = LocalDateTime.now();

    @JsonIgnore
    @LastModifiedDate
    private LocalDateTime lastModifiedTime = LocalDateTime.now();

    public Resume(@NonNull String resume_name, @NonNull User user){
        this.user = user;
        this.experiences = new ArrayList<>();
        this.education = new Education();
        this.header = new Header();
        this.sections = null;
        this.name = resume_name;
    }

    public Resume(@NonNull Resume originalResume, @NonNull String newName){
        if(originalResume.getUser().maxResumeLimitReached()){
            throw new MaxResumesReachedException("You have reached the maximum number of resumes you can create");
        }
        Education originalEducation = originalResume.getEducation();
        Header originalHeader = originalResume.getHeader();
        List<Experience> originalExperiences = originalResume.getExperiences();
        List<ResumeSection> originalSections = originalResume.getSections();
        this.name = newName;
        this.user = originalResume.getUser();
        // We are creating new objects here, because we do not want them to be a reference to the original ones.
        this.education = new Education(originalEducation.getSchoolName(),
                originalEducation.getRelevantCoursework());
        this.header = new Header(originalHeader.getNumber(), originalHeader.getName(), originalHeader.getEmail());
        this.experiences = originalExperiences.stream().map(
                experience -> new Experience(experience.getCompanyName(), experience.getTechnologyList(),
                        experience.getTimePeriod(), experience.getBullets())
        ).toList();
        this.sections = originalSections.stream().map(
                section -> new ResumeSection(section.getTitle(), section.getBullets())
        ).toList();
        //Necessary in order for cascading to work properly
        this.getUser().getResumes().add(this);
        this.creationTime = LocalDateTime.now();
        // Technically, the LocalDataTime.now() call will be different from the one above,
        // and we want these dates to match initially
        this.lastModifiedTime = this.creationTime;

    }

    public void addExperience(Experience experience){
        experiences.add(experience);
    }

    public void addExperience(int index, Experience experience){
        experiences.add(index, experience);
    }

    public void addSection(ResumeSection section){
        if (sections == null){
            sections = new ArrayList<>();
            sections.add(section);
        }
        else{
            sections.add(section);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(header.toString() + education.toString());
        sb.append("\nEXPERIENCES:\n");
        experiences.forEach(experience -> sb.append(experience.toString())
        );
        sb.append("\nSECTIONS:\n");
        sections.forEach(section -> sb.append(section.toString()));
        return sb.toString();
    }
}
