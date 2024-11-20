package com.rebuild.backend.model.entities.profile_entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.utils.converters.DurationToStringConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "profile_experiences", uniqueConstraints = {
        @UniqueConstraint(name = "uk_profile_experiences", columnNames = {
                "company_name", "profile_id"
        })
})
@Data
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
public class ProfileExperience {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(
            name = "id"
    )
    @JsonIgnore
    private UUID id;

    @Column(name = "company_name", nullable = false)
    @NonNull
    private String companyName;

    @ElementCollection
    @CollectionTable(name = "profile_technologies", joinColumns = @JoinColumn(name = "experience_id"))
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

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_profile_experience_id"))
    @JsonIgnore
    private UserProfile profile;
}
