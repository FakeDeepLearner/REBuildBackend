package com.rebuild.backend.model.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Data
@Table(name = "enable_tokens")
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
public class EnableAccountToken {

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
