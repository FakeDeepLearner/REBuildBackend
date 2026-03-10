package com.rebuild.backend.service.util_services;

import com.rebuild.backend.model.entities.forum_entities.ForumPost;
import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.model.forms.forum_forms.ForumSpecsForm;
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

    public List<UUID> executeResumeSearch(String nameToSearch)
    {
        SearchSession searchSession = Search.session(entityManager);
        return searchSession.search(Resume.class)
                .select(f -> f.id(UUID.class))
                .where(f -> new NullSafeQuerySearchBuilder(f).
                        nullSafeMatch("name", nameToSearch).
                        getResult()
                )
                .sort(f -> f.composite(
                        composite -> {
                            composite.add(f.field("creationTime").desc());
                            composite.add(f.field("lastModifiedTime").desc());
                        }
                ))
                .fetchAllHits();


    }

    public List<UUID> executePostSearch(ForumSpecsForm specsForm){
        SearchSession searchSession = Search.session(entityManager);
        return searchSession.search(ForumPost.class)
                .select(f -> f.id(UUID.class))
                .where(f -> new NullSafeQuerySearchBuilder(f).
                        nullSafeMatch("title", specsForm.titleContains()).
                        nullSafeMatch("content", specsForm.bodyContains()).
                        getResult()

                )
                .sort(f -> f.composite(
                            composite -> {
                            composite.add(f.field("creationDate").desc());
                            composite.add(f.field("lastModificationDate").desc());
                            }
                            ))
                .fetchAllHits();

    }


    public List<UUID> executeUserSearch(String exampleName)
    {
        SearchSession searchSession = Search.session(entityManager);

        return searchSession.search(ForumPost.class)
                .select(f -> f.id(UUID.class)).
                where(f -> new NullSafeQuerySearchBuilder(f)
                        .nullSafeMatch("forumUsername", exampleName).getResult()).
                fetchAllHits();
    }
}
