package com.rebuild.backend.model.dtos.forum_dtos;

import com.rebuild.backend.model.entities.messaging_and_friendship_entities.AbstractChat;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.Message;

public record NewMessageDTO(AbstractChat newChat, Message newMessage){
}
