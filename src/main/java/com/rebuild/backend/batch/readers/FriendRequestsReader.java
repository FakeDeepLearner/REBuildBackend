package com.rebuild.backend.batch.readers;

import com.rebuild.backend.model.entities.messaging_and_friendship_entities.FriendRequest;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;

public class FriendRequestsReader extends JpaPagingItemReader<FriendRequest> {

    public FriendRequestsReader(EntityManagerFactory entityManagerFactory,
                                Instant timestampCutoff) throws Exception {
        setEntityManagerFactory(entityManagerFactory);
        // Since we are looking for the last 10 minutes, a FriendRequest is within the range we are looking for if its
        // timestamp is >= the timestamp of the last 10 minutes.
        setQueryString("""
        SELECT req FROM FriendRequest req
        WHERE req.creationTimestamp>=:timestampCutoff
        """);
        setParameterValues(Map.of("timestampCutoff", timestampCutoff));
        setPageSize(100);
        setTransacted(true);
        setSaveState(false);
        afterPropertiesSet();
    }
}
