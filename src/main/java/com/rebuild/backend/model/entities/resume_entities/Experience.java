package com.rebuild.backend.model.entities.resume_entities;

import com.rebuild.backend.utils.converters.DurationToStringConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "experiences")
@Data
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
public class Experience {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(
            name = "id"
    )
    private UUID id;

    @Column(name = "company_name", nullable = false)
    @NonNull
    private String companyName;

    @ElementCollection
    @CollectionTable(name = "technologies", joinColumns = @JoinColumn(name = "experience_id"))
    @Column(nullable = false)
    @NonNull
    private List<String> technologyList;

    @Column(name = "time_period", nullable = false)
    @NonNull
    @Convert(converter = DurationToStringConverter.class)
    private Duration timePeriod;

    @ElementCollection
    @CollectionTable(name = "bullets", joinColumns = @JoinColumn(name = "experience_id"))
    @Column(name = "bullets", nullable = false)
    @NonNull
    private List<String> bullets;

    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false, referencedColumnName = "id",
        foreignKey = @ForeignKey(name = "exp_fk_resume_id"))
    @NonNull
    private Resume resume;

}
