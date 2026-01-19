package com.rebuild.backend.model.entities.resume_entities.search_entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Data
public class EducationSearchProperties {

    @NonNull
    private String schoolNameSearch;

    @NonNull
    private String courseworkSearch;
}
