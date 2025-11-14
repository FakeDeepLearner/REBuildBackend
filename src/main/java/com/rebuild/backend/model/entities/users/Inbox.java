package com.rebuild.backend.model.entities.users;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.FriendRequest;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "inboxes", indexes = {
        @Index(columnList = "user_id"),
        @Index(columnList = "request_id"),
        @Index(columnList = "message_id")
})
@Data
@RequiredArgsConstructor
@NoArgsConstructor
public class Inbox {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, cascade = {
            CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH
    })
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    @JsonIgnore
    @NonNull
    private User associatedUser;

    @OneToMany(mappedBy = "associatedInbox", fetch = FetchType.LAZY, cascade = {
     CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH
    })
    private List<FriendRequest> friendRequests = new ArrayList<>();


    public void addFriendRequest(FriendRequest friendRequest) {
        friendRequests.add(friendRequest);
    }


}
