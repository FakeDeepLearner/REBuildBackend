package com.rebuild.backend.model.entities.superclasses;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.utils.converters.database_converters.YearMonthDatabaseConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@MappedSuperclass
@Data
@RequiredArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class SuperclassSectionEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JsonIgnore
    protected UUID id;

    @Column(nullable = false, name = "title")
    @NonNull
    protected String title;

    @ElementCollection
    @CollectionTable(name = "section_tools", joinColumns = @JoinColumn(name = "section_tool_id"))
    @NonNull
    protected List<String> toolsUsed;

    @Column(nullable = false, name = "location")
    @NonNull
    protected String location;

    @Column(name = "start_date")
    @NonNull
    @Convert(converter = YearMonthDatabaseConverter.class)
    protected YearMonth startDate;

    @Column(name = "start_date")
    @NonNull
    @Convert(converter = YearMonthDatabaseConverter.class)
    protected YearMonth endDate;

    @ElementCollection
    @CollectionTable(name = "section_bullets",
            joinColumns = @JoinColumn(name = "section_bullet_id"))
    @NonNull
    protected List<String> bullets;
}
