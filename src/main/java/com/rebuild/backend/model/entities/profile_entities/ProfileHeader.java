package com.rebuild.backend.model.entities.profile_entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.model.entities.superclasses.SuperclassHeader;

import jakarta.persistence.*;
import lombok.*;



@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "profile_headers")
@Data
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
public class ProfileHeader extends SuperclassHeader {

    public ProfileHeader(String number, String firstName, String lastName, String email){
        super(number, firstName, lastName, email);
    }

    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_profile_header_id"))
    @JsonIgnore
    private UserProfile profile;
}
