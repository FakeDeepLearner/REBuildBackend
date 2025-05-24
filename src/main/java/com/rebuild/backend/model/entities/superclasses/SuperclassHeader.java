package com.rebuild.backend.model.entities.superclasses;

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
public abstract class SuperclassHeader {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    protected UUID id;

    @NonNull
    @Column(nullable = false, name = "phone_number")
    @Convert(converter = DatabaseEncryptor.class)
    protected String number;

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
