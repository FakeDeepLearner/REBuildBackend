package com.rebuild.backend.model.entities.resume_entities.search_entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Embeddable
@RequiredArgsConstructor
@NoArgsConstructor
@Data
public class ProjectSearchProperties {

    @NonNull
    private String projectNameSearch;

    @NonNull
    private String projectTechnologyListSearch;

    @NonNull
    private String projectBulletsSearch;
}
