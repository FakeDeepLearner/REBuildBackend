package com.rebuild.backend.model.entities.chat_entities;

import com.rebuild.backend.model.entities.user_entities.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;

import java.util.UUID;

@Entity
@Table(name = "chat_search_infos")
@NoArgsConstructor
@RequiredArgsConstructor
public class ChatSearchInformation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(cascade = {
            CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH
    })
    @JoinColumn(name = "chat_id", referencedColumnName = "id")
    @NonNull
    private GroupChat associatedChat;

    @Column(name = "descripton_embedding")
    @JdbcTypeCode(SqlTypes.VECTOR)
    @Array(length = 1536)
    private float[] descriptionEmbedding;

}
