package com.rebuild.backend.model.entities.superclasses;

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

@MappedSuperclass
@Data
@RequiredArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class SuperclassEducation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JsonIgnore
    protected UUID id;

    @NonNull
    @Convert(converter = DatabaseEncryptor.class)
    protected String schoolName;

    @NonNull
    @ElementCollection
    @CollectionTable(name = "courses", joinColumns = @JoinColumn(name = "education_id"))
    protected List<String> relevantCoursework;

    @Column(name = "location", nullable = false)
    @NonNull
    protected String location;

    @Column(name = "start_date", nullable = false)
    @NonNull
    @JsonSerialize(using = YearMonthSerializer.class)
    @Convert(converter = YearMonthDatabaseConverter.class)
    protected YearMonth startDate;

    @Column(name = "end_date", nullable = false)
    @NonNull
    @JsonSerialize(using = YearMonthSerializer.class)
    @Convert(converter = YearMonthDatabaseConverter.class)
    protected YearMonth endDate;
}
