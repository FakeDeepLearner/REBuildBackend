package com.rebuild.backend.model.dtos.forum_dtos;

import com.rebuild.backend.model.entities.messaging_and_friendship_entities.Chat;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.Message;

public record NewMessageDTO(Chat newChat, Message newMessage){
}
