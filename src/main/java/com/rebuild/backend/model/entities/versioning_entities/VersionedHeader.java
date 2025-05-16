package com.rebuild.backend.model.entities.versioning_entities;

import com.rebuild.backend.model.entities.resume_entities.PhoneNumber;

import com.rebuild.backend.model.entities.superclasses.SuperclassHeader;

import jakarta.persistence.*;
import lombok.*;



@Table(name = "versioned_headers")
@Entity
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class VersionedHeader extends SuperclassHeader {

    public VersionedHeader(PhoneNumber number, String firstName, String lastName, String email){
        super(number, firstName, lastName, email);
    }

    @OneToOne(cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE
    })
    @JoinColumn(name = "version_id", referencedColumnName = "id", nullable = false,
            foreignKey = @ForeignKey(name = "head_fk_version_id"))
    private ResumeVersion associatedVersion;
}
