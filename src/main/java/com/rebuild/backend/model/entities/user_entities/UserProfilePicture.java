package com.rebuild.backend.model.entities.user_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.model.entities.util_entitites.base_entities.AbstractPicture;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_profile_pictures")
@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
public class UserProfilePicture extends AbstractPicture {

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "profile_id", referencedColumnName = "id")
    @JsonIgnore
    private UserProfile associatedProfile;

    public UserProfilePicture(String bucketName, String keyName, UserProfile profile)
    {
        super(bucketName, keyName);
        this.associatedProfile = profile;
    }
}
