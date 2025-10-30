package com.rebuild.backend.config.other;

import com.rebuild.backend.model.entities.forum_entities.ForumPost;
import com.rebuild.backend.model.entities.resume_entities.Resume;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.massindexing.MassIndexer;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IndexExistingDataConfig {
    private final EntityManager entityManager;

    @Autowired
    public IndexExistingDataConfig(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @PostConstruct
    public void indexPostData() throws InterruptedException {
        SearchSession session = Search.session(entityManager);
        MassIndexer massIndexer = session.massIndexer(ForumPost.class);

        massIndexer.
                batchSizeToLoadObjects(5).
                threadsToLoadObjects(5).
                dropAndCreateSchemaOnStart(true).
                startAndWait();
    }

    @PostConstruct
    public void indexResumeData() throws InterruptedException {
        SearchSession session = Search.session(entityManager);
        MassIndexer massIndexer = session.massIndexer(Resume.class);

        massIndexer.
                batchSizeToLoadObjects(10).
                threadsToLoadObjects(8).
                dropAndCreateSchemaOnStart(true).
                startAndWait();
    }

    @PostConstruct
    public void indexUser() throws InterruptedException {
        SearchSession session = Search.session(entityManager);
        MassIndexer massIndexer = session.massIndexer(User.class);

        massIndexer.
                batchSizeToLoadObjects(10).
                threadsToLoadObjects(8).
                dropAndCreateSchemaOnStart(true).
                startAndWait();
    }

}
