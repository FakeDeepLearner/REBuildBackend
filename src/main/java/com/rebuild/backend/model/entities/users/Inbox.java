package com.rebuild.backend.model.entities.users;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.FriendRequest;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.Message;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "inboxes")
@Data
@RequiredArgsConstructor
@NoArgsConstructor
public class Inbox {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JsonIgnore
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, cascade = {
            CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH
    })
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    @JsonIgnore
    @NonNull
    private User associatedUser;


    @OneToMany(fetch = FetchType.LAZY, cascade = {
     CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH
    })
    @JoinColumn(name = "inbox_id")
    @OrderColumn(name = "insert_position")
    private List<FriendRequest> pendingRequests = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = {
            CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH
    })
    @JoinColumn(name = "inbox_id")
    @OrderColumn(name = "insert_position")
    private List<Message> incomingMessages = new ArrayList<>();


}
