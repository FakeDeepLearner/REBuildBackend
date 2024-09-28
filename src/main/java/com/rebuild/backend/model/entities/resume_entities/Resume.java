package com.rebuild.backend.model.entities.resume_entities;

import com.rebuild.backend.exceptions.resume_exceptions.MaxResumesReachedException;
import com.rebuild.backend.model.entities.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "resumes", uniqueConstraints = {
        @UniqueConstraint(name = "uk_same_user_resume_name", columnNames = {"user_id", "name"})
})
@Data
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
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


    public Resume(@NonNull String resume_name, @NonNull User user){
        this.user = user;
        this.experiences = new ArrayList<>();
        this.education = new Education();
        this.header = new Header();
        this.sections = null;
        this.name = resume_name;
    }

    public Resume(@NonNull Resume originalResume){
        if(originalResume.getUser().maxResumeLimitReached()){
            throw new MaxResumesReachedException("You have reached the maximum number of resumes you can create");
        }
        Education originalEducation = originalResume.getEducation();
        Header originalHeader = originalResume.getHeader();
        List<Experience> originalExperiences = originalResume.getExperiences();
        List<ResumeSection> originalSections = originalResume.getSections();
        this.name = originalResume.getName();
        this.user = originalResume.getUser();
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

    }

    public void addExperience(Experience experience){
        experiences.add(experience);
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
}
