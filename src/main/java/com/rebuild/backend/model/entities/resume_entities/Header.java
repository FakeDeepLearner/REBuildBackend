package com.rebuild.backend.model.entities.resume_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.utils.converters.database_converters.PhoneAndStringDatabaseConverter;
import com.rebuild.backend.utils.converters.encrypt.DatabaseEncryptor;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@Entity
@Table(name = "headers")
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
public class Header {

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
    @JoinColumn(name = "resume_id", referencedColumnName = "id",
    foreignKey = @ForeignKey(name = "head_fk_resume_id"))
    private Resume resume;

    @OneToOne(fetch = FetchType.LAZY, cascade = {
            CascadeType.MERGE,
            CascadeType.PERSIST
    })
    @JoinColumn(name = "associated_version_id", referencedColumnName = "id")
    @JsonIgnore
    private ResumeVersion associatedVersion;

    @Override
    public String toString() {
        return "HEADER:\n" +
                "\tPhone Number: " + number.fullNumber() + "\n" +
                "\tName: " + firstName + " " + lastName + "\n" +
                "\tEmail: " + email + "\n\n\n";
    }


}
