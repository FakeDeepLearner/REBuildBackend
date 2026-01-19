package com.rebuild.backend.model.entities.resume_entities.search_entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.io.Serial;
import java.io.Serializable;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Data
public class EducationSearchProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 27L;

    @NonNull
    private String schoolNameSearch;

    @NonNull
    private String courseworkSearch;
}
