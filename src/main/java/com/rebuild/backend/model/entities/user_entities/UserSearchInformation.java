package com.rebuild.backend.model.entities.user_entities;

import jakarta.persistence.*;
import jdk.jfr.Enabled;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.Array;
import org.hibernate.annotations.GeneratedColumn;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.util.UUID;

@Entity
@Table(name = "user_search_infos")
@RequiredArgsConstructor
@NoArgsConstructor
public class UserSearchInformation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(cascade = {
            CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH
    })
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @NonNull
    private User associatedUser;

    @Column(name = "biography_embedding")
    @JdbcTypeCode(SqlTypes.VECTOR)
    @Array(length = 1536)
    private float[] biographyEmbedding;

    @Column(name = "location", columnDefinition = "geography(Point, 4326)")
    @JdbcTypeCode(SqlTypes.GEOGRAPHY)
    private Point locationPoint;
}
