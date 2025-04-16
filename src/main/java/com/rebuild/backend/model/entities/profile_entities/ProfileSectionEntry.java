package com.rebuild.backend.model.entities.profile_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.utils.converters.database_converters.YearMonthDatabaseConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "profile_section_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
public class ProfileSectionEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JsonIgnore
    private UUID id;

    @Column(nullable = false, name = "title")
    @NonNull
    private String title;

    @ElementCollection
    @CollectionTable(name = "resume_section_tools", joinColumns = @JoinColumn(name = "section_entry_id"))
    @NonNull
    private List<String> toolsUsed;

    @Column(nullable = false, name = "location")
    @NonNull
    private String location;

    @Column(name = "start_date")
    @NonNull
    @Convert(converter = YearMonthDatabaseConverter.class)
    private YearMonth startDate;

    @Column(name = "start_date")
    @NonNull
    @Convert(converter = YearMonthDatabaseConverter.class)
    private YearMonth endDate;

    @ElementCollection
    @CollectionTable(name = "resume_section_bullets",
            joinColumns = @JoinColumn(name = "section_entry_id"))
    @NonNull
    private List<String> bullets;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "associated_section_id", nullable = false, referencedColumnName = "id")
    @JsonIgnore
    private ProfileSection associatedSection;


}
