package com.rebuild.backend.model.entities.profile_entities;


import com.rebuild.backend.model.entities.resume_entities.PhoneNumber;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "profile_headers")
@Data
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
public class ProfileHeader {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "countryCode", column = @Column(name = "phone_country_code")),
            @AttributeOverride(name = "areaCode", column = @Column(name = "phone_area_code")),
            @AttributeOverride(name = "restOfNumber", column = @Column(name = "phone_remainder")),
    })
    @NonNull
    private PhoneNumber number;


    @Column(name = "name", nullable = false)
    @NonNull
    private String name;

    @Column(name = "email", nullable = false)
    @NonNull
    private String email;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "profile_id", referencedColumnName = "id", foreignKey =
    @ForeignKey(name = "fk_profile_header_id"))
    private UserProfile profile;
}
