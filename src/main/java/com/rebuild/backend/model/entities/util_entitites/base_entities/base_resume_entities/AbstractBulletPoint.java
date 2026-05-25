package com.rebuild.backend.model.entities.util_entitites.base_entities.base_resume_entities;

import com.rebuild.backend.model.entities.util_entitites.Auditable;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@MappedSuperclass
@Getter
@Setter
@RequiredArgsConstructor
@NoArgsConstructor
@AllArgsConstructor
public abstract class AbstractBulletPoint extends Auditable{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    protected UUID id;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSONB",  nullable = false)
    @NonNull
    protected String text;
}
