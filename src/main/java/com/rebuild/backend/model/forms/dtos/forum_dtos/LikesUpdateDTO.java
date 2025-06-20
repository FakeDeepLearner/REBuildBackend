package com.rebuild.backend.model.forms.dtos.forum_dtos;

import com.rebuild.backend.model.entities.enums.LikeType;

import java.util.UUID;

public record LikesUpdateDTO(UUID targetObjectId, LikeType typeOfTarget, long numItemsRead) {
}
