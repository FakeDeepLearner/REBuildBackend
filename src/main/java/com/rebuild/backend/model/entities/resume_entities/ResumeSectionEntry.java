package com.rebuild.backend.model.entities.resume_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.utils.converters.database_converters.YearMonthDatabaseConverter;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "resume_section_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
public class ResumeSectionEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JsonIgnore
    private UUID id;

    @Column(nullable = false, name = "title")
    @NonNull
    private String title;

    @NonNull
    @ElementCollection
    @CollectionTable(name = "resume_section_tools", joinColumns = @JoinColumn(name = "section_entry_id"))
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

    @NonNull
    @ElementCollection
    @CollectionTable(name = "resume_section_bullets", joinColumns = @JoinColumn(name = "section_entry_id"))
    private List<String> bullets;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "associated_section_id", nullable = false, referencedColumnName = "id")
    @JsonIgnore
    private ResumeSection associatedSection;

    @Override
    public String toString() {
        return "\tSECTION_ENTRY:\n" +
                "\t\tTitle: " + title + "\n" +
                "\t\tTools: " + toolsUsed + "\n" +
                "\t\tLocation: " + location + "\n" +
                "\t\tBullets: " + bullets + "\n";
    }
}
