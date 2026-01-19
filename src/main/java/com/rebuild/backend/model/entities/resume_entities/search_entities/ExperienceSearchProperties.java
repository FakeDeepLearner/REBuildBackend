package com.rebuild.backend.model.entities.resume_entities.search_entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Embeddable
@RequiredArgsConstructor
@NoArgsConstructor
@Data
public class ExperienceSearchProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 25L;

    @NonNull
    private String companySearch;

    @NonNull
    private String experienceBulletsSearch;

    @NonNull
    private String experienceTechnologiesSearch;
}
