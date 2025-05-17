package com.rebuild.backend.model.entities.superclasses;

import com.rebuild.backend.model.entities.resume_entities.PhoneNumber;
import com.rebuild.backend.utils.converters.database_converters.PhoneAndStringDatabaseConverter;
import com.rebuild.backend.utils.converters.encrypt.DatabaseEncryptor;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@MappedSuperclass
@Data
@RequiredArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public abstract class SuperclassHeader{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    protected UUID id;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "countryCode", column = @Column(name = "phone_country_code")),
            @AttributeOverride(name = "areaCode", column = @Column(name = "phone_area_code")),
            @AttributeOverride(name = "restOfNumber", column = @Column(name = "phone_remainder")),
    })
    @NonNull
    @Convert(converter = PhoneAndStringDatabaseConverter.class)
    protected PhoneNumber number;

    @Column(name = "first_name", nullable = false)
    @NonNull
    @Convert(converter = DatabaseEncryptor.class)
    protected String firstName;

    @Column(name = "last_name", nullable = false)
    @NonNull
    @Convert(converter = DatabaseEncryptor.class)
    protected String lastName;

    @Column(name = "email", nullable = false)
    @NonNull
    @Convert(converter = DatabaseEncryptor.class)
    protected String email;
}
