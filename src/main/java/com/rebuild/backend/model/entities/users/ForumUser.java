package com.rebuild.backend.model.entities.users;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Entity
@DiscriminatorValue("FORUM_USER")
@Data
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
public class ForumUser extends User{
    private String forumUsername;

    private String forumPassword;
}
