package com.rebuild.backend.model.forms.dtos.forum_dtos;

import com.rebuild.backend.model.entities.forum_entities.LikeType;

import java.util.UUID;

public record LikesUpdateDTO(UUID targetObjectId, LikeType typeOfTarget, long numItemsRead) {
}
