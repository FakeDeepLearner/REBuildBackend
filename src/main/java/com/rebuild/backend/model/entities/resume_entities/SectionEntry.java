package com.rebuild.backend.model.entities.resume_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.rebuild.backend.utils.converters.database_converters.YearMonthDatabaseConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;


@Entity
@Table(name = "resume_section_entries")
@Data
@NoArgsConstructor
@RequiredArgsConstructor
@AllArgsConstructor
public class SectionEntry implements ResumeProperty {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JsonIgnore
    private UUID id;

    @Column(nullable = false, name = "title")
    @NonNull
    private String title;

    @ElementCollection
    @CollectionTable(name = "section_tools", joinColumns = @JoinColumn(name = "section_tool_id"))
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
    @CollectionTable(name = "section_bullets",
            joinColumns = @JoinColumn(name = "section_bullet_id"))
    @NonNull
    private List<String> bullets;

    @Override
    public String toString() {
        return "\tSECTION_ENTRY:\n" +
                "\t\tTitle: " + title + "\n" +
                "\t\tTools: " + toolsUsed + "\n" +
                "\t\tLocation: " + location + "\n" +
                "\t\tBullets: " + bullets + "\n";
    }

    public static SectionEntry copy(SectionEntry other){
        return new SectionEntry(other.title, other.toolsUsed, other.location,
                other.startDate, other.endDate, other.bullets);
    }
}
