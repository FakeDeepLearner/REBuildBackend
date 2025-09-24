package com.rebuild.backend.model.entities.users;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "verification_records")
@RequiredArgsConstructor
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SentVerificationRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "channel", nullable = false)
    @NonNull
    private String channel;

    @Column(name = "to", nullable = false)
    @NonNull
    private String to;

    @Column(name = "sent_timestamp", nullable = false)
    @NonNull
    private ZonedDateTime timestamp;
}
