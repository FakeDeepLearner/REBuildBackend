package com.rebuild.backend.model.entities.users;

import com.rebuild.backend.utils.converters.database_converters.LocalDateTimeDatabaseConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "persistent_logins")
@RequiredArgsConstructor
@NoArgsConstructor
@AllArgsConstructor
@Data
public class RememberMeToken{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    @Column(nullable = false)
    @NonNull
    private String token;

    @Column(nullable = false, name = "last_used")
    @NonNull
    @Convert(converter = LocalDateTimeDatabaseConverter.class)
    private LocalDateTime expiryTime;

    @OneToOne(cascade = {CascadeType.MERGE,
            CascadeType.PERSIST,
            CascadeType.REFRESH}, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

}
