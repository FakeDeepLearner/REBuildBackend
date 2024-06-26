package com.rebuild.backend.model.entities.tokens;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table(name = "email_change_tokens")
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
public class EmailChangeToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NonNull
    private String token;

    @NonNull
    private String emailFor;

    @NonNull
    private LocalDateTime expiryTime;
}
