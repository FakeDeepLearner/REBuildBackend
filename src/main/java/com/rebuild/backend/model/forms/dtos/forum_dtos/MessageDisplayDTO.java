package com.rebuild.backend.model.forms.dtos.forum_dtos;

import java.time.Instant;
import java.time.LocalDateTime;

public record MessageDisplayDTO(String messageContent, Instant messageTime,
                                boolean displayOnTheRight) {
}

