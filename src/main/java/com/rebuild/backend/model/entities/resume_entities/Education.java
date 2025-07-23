package com.rebuild.backend.model.entities.resume_entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import com.rebuild.backend.utils.converters.database_converters.YearMonthDatabaseConverter;
import com.rebuild.backend.utils.converters.encrypt.DatabaseEncryptor;
import com.rebuild.backend.utils.serializers.YearMonthSerializer;
import jakarta.persistence.*;
import lombok.*;


import java.time.YearMonth;
import java.util.List;
import java.util.UUID;



@Entity
@Table(name = "educations")
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
@Data
public class Education implements ResumeProperty {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JsonIgnore
    private UUID id;

    @NonNull
    @Convert(converter = DatabaseEncryptor.class)
    private String schoolName;

    @NonNull
    @ElementCollection
    @CollectionTable(name = "courses", joinColumns = @JoinColumn(name = "education_id"))
    private List<String> relevantCoursework;

    @Column(name = "location", nullable = false)
    @NonNull
    private String location;

    @Column(name = "start_date", nullable = false)
    @NonNull
    @JsonSerialize(using = YearMonthSerializer.class)
    @Convert(converter = YearMonthDatabaseConverter.class)
    private YearMonth startDate;

    @Column(name = "end_date", nullable = false)
    @NonNull
    @JsonSerialize(using = YearMonthSerializer.class)
    @Convert(converter = YearMonthDatabaseConverter.class)
    private YearMonth endDate;


    public String toString() {
        return "EDUCATION:\n" +
                "\tSchool Name: " + schoolName + "\n" +
                "\tCoursework: " + relevantCoursework + "\n" +
                "\tLocation: " + location +
                "\n\n\n";
    }
}
