package com.rebuild.backend.model.entities.messaging_and_friendship_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.model.entities.user_entities.UserProfile;
import com.rebuild.backend.model.entities.util_entitites.base_entities.AbstractPicture;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "group_chat_pictures")
@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
public class GroupChatPicture extends AbstractPicture {

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "associated_chat_id", referencedColumnName = "id")
    @JsonIgnore
    private GroupChat associatedChat;

    public GroupChatPicture(String bucketName, String keyName, GroupChat associatedChat)
    {
        super(bucketName, keyName);
        this.associatedChat = associatedChat;
    }
}
