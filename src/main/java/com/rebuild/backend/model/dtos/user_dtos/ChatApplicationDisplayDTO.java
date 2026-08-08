package com.rebuild.backend.model.dtos.user_dtos;

import java.util.UUID;

public record ChatApplicationDisplayDTO(UUID id, String chatName, String content,
                                        String createdTime) {

}
