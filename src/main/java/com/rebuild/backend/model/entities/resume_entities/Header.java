package com.rebuild.backend.model.entities.resume_entities;


import com.fasterxml.jackson.annotation.JsonIgnore;

import com.rebuild.backend.utils.converters.encrypt.DatabaseEncryptor;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;


@Entity
@Table(name = "headers")
@Data
@AllArgsConstructor
@RequiredArgsConstructor
@NoArgsConstructor
public class Header implements ResumeProperty {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JsonIgnore
    private UUID id;

    @NonNull
    @Column(nullable = false, name = "phone_number")
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

    @Override
    public String toString() {
        return "HEADER:\n" +
                "\tPhone Number: " + number + "\n" +
                "\tName: " + firstName + " " + lastName + "\n" +
                "\tEmail: " + email + "\n\n\n";
    }


}
