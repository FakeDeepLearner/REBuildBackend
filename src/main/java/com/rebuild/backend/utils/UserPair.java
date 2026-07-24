package com.rebuild.backend.utils;

import com.rebuild.backend.model.entities.user_entities.User;

import java.util.UUID;

public record UserPair(User user1, User user2) {

    public UUID lowId()
    {
        UUID user1Id = user1.getId();
        UUID user2Id = user2.getId();

        if (user1Id.compareTo(user2Id) < 0)
        {
            return user1Id;
        }
        return user2Id;
    }

    public UUID highId()
    {
        UUID user1Id = user1.getId();
        UUID user2Id = user2.getId();

        //This being bigger than 0 means that the user 1 id is larger, so we return that.
        //The same reasoning applies for the method abooe, just in reverse.
        if (user1Id.compareTo(user2Id) > 0)
        {
            return user1Id;
        }
        return user2Id;
    }
}
