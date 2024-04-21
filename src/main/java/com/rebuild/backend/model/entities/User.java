package com.rebuild.backend.model.entities;

import com.rebuild.backend.model.entities.Resume;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "uk_username", columnNames = {"username"}),
        @UniqueConstraint(name = "uk_email", columnNames = {"email"})
})
@RequiredArgsConstructor
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(
            name = "id"
    )
    private UUID id;

    @Column(
            nullable = false,
            name = "username"
    )
    @NonNull
    private String username;


    @Column(
            nullable = false,
            name = "password"
    )
    @NonNull
    private String password;


    @Column(
            nullable = false,
            name = "email"
    )
    @NonNull
    private String email;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Resume> resumes;


}
