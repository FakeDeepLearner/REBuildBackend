package com.rebuild.backend.model.entities.util_entitites.base_entities;

import com.rebuild.backend.model.entities.util_entitites.Auditable;
import com.rebuild.backend.model.responses.resume_responses.EducationResponse;
import com.rebuild.backend.utils.StringUtil;
import com.rebuild.backend.utils.database_utils.DatabaseEncryptor;
import com.rebuild.backend.utils.database_utils.YearMonthDatabaseConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@MappedSuperclass
@Getter
@Setter
@RequiredArgsConstructor
@NoArgsConstructor
@AllArgsConstructor
public abstract class AbstractEducation extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NonNull
    @Convert(converter = DatabaseEncryptor.class)
    protected String schoolName;

    @NonNull
    @ElementCollection
    @CollectionTable(name = "courses", joinColumns = @JoinColumn(name = "education_id", referencedColumnName = "id"))
    protected List<String> relevantCoursework;

    @Column(name = "location", nullable = false)
    @NonNull
    protected String location;

    @Column(name = "start_date", nullable = false)
    @NonNull
    @Convert(converter = YearMonthDatabaseConverter.class)
    protected YearMonth startDate;

    @Column(name = "end_date", nullable = false)
    @Convert(converter = YearMonthDatabaseConverter.class)
    protected YearMonth endDate;

    public EducationResponse toResponse(){
        return new EducationResponse(this.schoolName, this.relevantCoursework, this.location,
                StringUtil.transformYearMonth(this.startDate), StringUtil.transformYearMonth(this.endDate));
    }
}
