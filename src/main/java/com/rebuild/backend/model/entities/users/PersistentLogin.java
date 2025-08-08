package com.rebuild.backend.model.entities.users;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Table(name = "persistent_logins")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PersistentLogin {

    @Column(nullable = false, name = "username", length = 64)
    private String username;


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "series", length = 64)
    private String series;


    @Column(nullable = false, name = "token", length = 64)
    private String token;


    @Column(nullable = false, name = "last_used")
    private Timestamp last_used;
}
