package com.rebuild.backend.model.entities.resume_entities;


import com.fasterxml.jackson.annotation.JsonIgnore;

import com.rebuild.backend.model.entities.profile_entities.UserProfile;
import com.rebuild.backend.utils.StringUtil;
import com.rebuild.backend.utils.database_utils.DatabaseEncryptor;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.search.engine.backend.types.Searchable;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;


@Entity
@Table(name = "headers")
@Data
@AllArgsConstructor
@RequiredArgsConstructor
@NoArgsConstructor
public class Header implements Serializable {

    @Serial
    private static final long serialVersionUID = 2L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @NonNull
    @Column(name = "phone_number")
    @Convert(converter = DatabaseEncryptor.class)
    private String number;

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

    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", referencedColumnName = "id")
    @JsonIgnore
    private ResumeVersion version;

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "resume_id", referencedColumnName = "id")
    @JsonIgnore
    private Resume resume;


    public static Header copy(Header other)
    {
        return new Header(other.getNumber(), other.getFirstName(), other.getLastName(), other.getEmail());
    }

    public static Header sensitiveCopy(Header other)
    {
        return new Header(StringUtil.maskString(other.getNumber()), StringUtil.maskString(other.firstName),
                StringUtil.maskString(other.getLastName()), StringUtil.maskString(other.getEmail()));
    }


}
