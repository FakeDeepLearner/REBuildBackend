package com.rebuild.backend.service.util_services;

import com.rebuild.backend.model.entities.forum_entities.ForumPost;
import com.rebuild.backend.model.entities.forum_entities.PostSearchConfiguration;
import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.model.entities.resume_entities.ResumeSearchConfiguration;
import com.rebuild.backend.model.entities.user_entities.User;
import jakarta.persistence.EntityManager;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ElasticSearchService {

    private final EntityManager entityManager;

    public ElasticSearchService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<UUID> executeResumeSearch(ResumeSearchConfiguration searchConfiguration, User user)
    {
        SearchSession searchSession = Search.session(entityManager);
        List<UUID> matchedIds = searchSession.search(Resume.class)
                .select(f -> f.id(UUID.class))
                .where(f -> new NullSafeQuerySearchBuilder(f).
                        nullSafeMatch("userId", user.getId()).
                        nullSafeMatch("name", searchConfiguration.getResumeNameSearch()).
                        atLeast("creationTime", searchConfiguration.getCreationAfterCutoff()).
                        atMost("creationTime", searchConfiguration.getCreationBeforeCutoff()).
                        getResult()
                )
                .sort(f -> f.composite(
                        composite -> {
                            composite.add(f.field("creationTime").desc());
                            composite.add(f.field("lastModifiedTime").desc());
                        }
                ))
                .fetchAllHits();
        return matchedIds;


    }

    public List<UUID> executePostSearch(PostSearchConfiguration postSearchConfiguration){
        SearchSession searchSession = Search.session(entityManager);
        List<UUID> matchedIds = searchSession.search(ForumPost.class)
                .select(f -> f.id(UUID.class))
                .where(f -> new NullSafeQuerySearchBuilder(f).
                        nullSafeMatch("title", postSearchConfiguration.getTitleSearch()).
                        nullSafeMatch("content", postSearchConfiguration.getBodySearch()).
                        atLeast("creationDate", postSearchConfiguration.getCreationAfterCutoff()).
                        atMost("creationDate", postSearchConfiguration.getCreationBeforeCutoff()).
                        getResult()

                )
                .sort(f -> f.composite(
                            composite -> {
                            composite.add(f.field("creationDate").desc());
                            composite.add(f.field("lastModificationDate").desc());
                            }
                            ))
                .fetchAllHits();
            return matchedIds;

    }


    public List<UUID> executeUserSearch(String exampleName)
    {
        SearchSession searchSession = Search.session(entityManager);

        return searchSession.search(ForumPost.class)
                .select(f -> f.id(UUID.class)).
                where(f -> new NullSafeQuerySearchBuilder(f)
                        .nullSafeMatch("forumUsername", exampleName).getResult()).fetchAllHits();
    }
}
