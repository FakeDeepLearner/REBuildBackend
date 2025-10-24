package com.rebuild.backend.model.forms.dtos.forum_dtos;

import java.time.LocalDateTime;

public record MessageDisplayDTO(String messageContent, LocalDateTime messageTime,
                                boolean displayOnTheRight) {
}

