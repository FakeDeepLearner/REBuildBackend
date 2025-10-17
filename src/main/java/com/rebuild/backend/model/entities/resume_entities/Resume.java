package com.rebuild.backend.model.entities.resume_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.utils.converters.database_converters.LocalDateTimeDatabaseConverter;
import com.rebuild.backend.utils.converters.database_converters.DatabaseEncryptor;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.*;
import org.springframework.data.elasticsearch.annotations.Field;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "resumes", uniqueConstraints = {
        @UniqueConstraint(name = "uk_same_user_resume_name", columnNames = {"user_id", "name"})
}, indexes = {
        @Index(columnList = "header_id"),
        @Index(columnList = "education_id"),
        @Index(columnList = "experience_id"),
        @Index(columnList = "user_id"),
        @Index(columnList = "user_id, id")
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
@Indexed
public class Resume implements Serializable {

    public static final int MAX_VERSION_COUNT = 10;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(
            name = "id"
    )
    @GenericField
    private UUID id;

    @Column(name = "name", nullable = false)
    @NonNull
    @Convert(converter = DatabaseEncryptor.class)
    @FullTextField
    private String name;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "resume")
    @IndexedEmbedded
    private Header header;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "resume")
    @IndexedEmbedded
    private Education education;

    @OneToMany(fetch = FetchType.LAZY, cascade = {
            CascadeType.ALL
    }, orphanRemoval = true, mappedBy = "resume")
    @OrderBy("endDate DESC NULLS FIRST, startDate DESC")
    @IndexedEmbedded
    private List<Experience> experiences;

    @ManyToOne(cascade = {
            CascadeType.REFRESH,
            CascadeType.PERSIST,
            CascadeType.MERGE
    })
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_user_id"))
    @JsonIgnore
    private User user;

    @Column(name = "version_count", nullable = false)
    private int versionCount = 0;

    @JsonIgnore
    @Convert(converter = LocalDateTimeDatabaseConverter.class)
    @GenericField
    private LocalDateTime creationTime = LocalDateTime.now();

    @JsonIgnore
    @Convert(converter = LocalDateTimeDatabaseConverter.class)
    @GenericField
    private LocalDateTime lastModifiedTime = LocalDateTime.now();

    public Resume(@NonNull String resume_name, @NonNull User user){
        this.user = user;
        this.experiences = new ArrayList<>();
        this.education = new Education();
        this.header = new Header();
        this.name = resume_name;
    }

    public Resume(@NonNull Resume originalResume, @NonNull String newName){
        if(originalResume.getUser().maxResumeLimitReached()){
            throw new RuntimeException("You have reached the maximum number of resumes you can create");
        }
        Education originalEducation = originalResume.getEducation();
        Header originalHeader = originalResume.getHeader();
        List<Experience> originalExperiences = originalResume.getExperiences();
        this.name = newName;
        this.user = originalResume.getUser();
        // We are creating new objects here,
        // because we do not want them to be a reference to the original ones.
        this.education = new Education(originalEducation.getSchoolName(),
                originalEducation.getRelevantCoursework(),
                originalEducation.getLocation(),
                originalEducation.getStartDate(), originalEducation.getEndDate());
        this.header = new Header(originalHeader.getNumber(), originalHeader.getFirstName(),
                originalHeader.getLastName(),
                originalHeader.getEmail());
        this.experiences = originalExperiences.stream().map(
                experience -> new Experience(experience.getCompanyName(), experience.getTechnologyList(),
                        experience.getLocation(), experience.getExperienceTypes(),
                        experience.getStartDate(), experience.getEndDate(), experience.getBullets())
        ).toList();
        //Necessary in order for cascading to work properly
        this.user.getResumes().add(this);
        this.education.setResume(this);
        this.header.setResume(this);
        this.experiences.forEach(experience -> experience.setResume(this));
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


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(header.toString() + education.toString());
        sb.append("\nEXPERIENCES:\n");
        experiences.forEach(experience -> sb.append(experience.toString())
        );
        sb.append("\nSECTIONS:\n");
        return sb.toString();
    }

    public static Resume deepCopy(Resume originalResume){

        String copiedName = originalResume.getName();

        return new Resume(originalResume, copiedName);
    }
}
