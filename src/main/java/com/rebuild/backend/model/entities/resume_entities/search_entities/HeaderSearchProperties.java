package com.rebuild.backend.model.entities.resume_entities.search_entities;

import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Embeddable
@RequiredArgsConstructor
@NoArgsConstructor
@Data
public class HeaderSearchProperties {

    @NonNull
    private String firstNameSearch;

    @NonNull
    private String lastNameSearch;

}
