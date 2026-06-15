package com.rebuild.backend.model.entities.util_entitites.base_entities;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.*;

import java.util.UUID;

@MappedSuperclass
@Getter
@Setter
@RequiredArgsConstructor
@NoArgsConstructor
@AllArgsConstructor
public class AbstractPicture {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    protected UUID id;

    @NonNull
    protected String bucketName;

    @NonNull
    protected String keyName;
}
