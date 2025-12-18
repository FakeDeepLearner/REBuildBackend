package com.rebuild.backend.batch.readers;

import com.rebuild.backend.model.forms.dtos.forum_dtos.LikesUpdateDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.item.database.JpaPagingItemReader;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;

public class LikeUpdateBatchReader extends JpaPagingItemReader<LikesUpdateDTO> {

    public LikeUpdateBatchReader(EntityManagerFactory entityManagerFactory,
                                 Instant timestampCutoff) throws Exception {

        setEntityManagerFactory(entityManagerFactory);
        setQueryString("""
        SELECT new com.rebuild.backend.model.forms.dtos.forum_dtos.LikesUpdateDTO(l.likedObjectId, l.likedObjectType, COUNT(l))
       \s
        FROM Like l\s
        WHERE l.likeTimestamp>=:timestampCutoff
       \s
        GROUP BY l.likedObjectId, l.likedObjectType
       \s""");
        setParameterValues(Map.of("timestampCutoff", timestampCutoff));
        setPageSize(50);
        setTransacted(true);
        setSaveState(false);
        afterPropertiesSet();
    }
}
