package com.rebuild.backend.batch.readers;

import com.rebuild.backend.model.forms.dtos.forum_dtos.LikesUpdateDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.item.database.JpaPagingItemReader;

import java.time.LocalDateTime;
import java.util.Map;

public class LikeUpdateBatchReader extends JpaPagingItemReader<LikesUpdateDTO> {

    public LikeUpdateBatchReader(EntityManagerFactory entityManagerFactory,
                                 LocalDateTime dateCutoff) throws Exception {

        setEntityManagerFactory(entityManagerFactory);
        setQueryString("""
        SELECT new com.rebuild.backend.model.forms.dtos.forum_dtos.LikesUpdateDTO(l.likedObjectId, l.likedObjectType, COUNT(l))
       \s
        FROM Like l\s
        WHERE l.likedDateTime > :dateCutoff
       \s
        GROUP BY l.likedObjectId, l.likedObjectType
       \s""");
        setParameterValues(Map.of("dateCutoff", dateCutoff));
        setPageSize(50);
        setTransacted(true);
        setSaveState(false);
        afterPropertiesSet();
    }
}
