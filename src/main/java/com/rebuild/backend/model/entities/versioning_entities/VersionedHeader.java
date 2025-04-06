package com.rebuild.backend.model.entities.versioning_entities;

import com.rebuild.backend.model.entities.resume_entities.PhoneNumber;
import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.utils.converters.database_converters.PhoneAndStringDatabaseConverter;
import com.rebuild.backend.utils.converters.encrypt.DatabaseEncryptor;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Table(name = "versioned_headers")
@Entity
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
public class VersionedHeader {

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
    @Convert(converter = PhoneAndStringDatabaseConverter.class)
    private PhoneNumber number;

    @Column(name = "first_name", nullable = false)
    @NonNull
    @Convert(converter = DatabaseEncryptor.class)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    @NonNull
    @Convert(converter = DatabaseEncryptor.class)
    private String lastName;

    @Column(name = "email", nullable = false)
    @NonNull
    @Convert(converter = DatabaseEncryptor.class)
    private String email;

    @OneToOne(cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE
    })
    @JoinColumn(name = "version_id", referencedColumnName = "id", nullable = false,
            foreignKey = @ForeignKey(name = "head_fk_version_id"))
    private ResumeVersion associatedVersion;
}
