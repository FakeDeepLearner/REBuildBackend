package com.rebuild.backend.utils.batch.readers;

import com.rebuild.backend.model.entities.messaging_and_friendship_entities.FriendRequest;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

public class FriendRequestsReader extends JpaPagingItemReader<FriendRequest> {

    public FriendRequestsReader(EntityManagerFactory entityManagerFactory,
                                LocalDateTime dateCutoff) throws Exception {
        setEntityManagerFactory(entityManagerFactory);
        setQueryString("""
        SELECT req FROM FriendRequest req
        WHERE req.status='PENDING' AND req.creationDate<=:dateCutoff
        """);
        setParameterValues(Map.of("dateCutoff", dateCutoff));
        setPageSize(100);
        setTransacted(true);
        setSaveState(false);
        afterPropertiesSet();
    }
}
