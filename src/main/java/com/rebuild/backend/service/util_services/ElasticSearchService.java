package com.rebuild.backend.service.util_services;

import com.rebuild.backend.model.entities.forum_entities.ForumPost;
import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.model.forms.dtos.forum_dtos.SearchResultDTO;
import com.rebuild.backend.model.forms.forum_forms.ForumSpecsForm;
import com.rebuild.backend.model.forms.resume_forms.ResumeSpecsForm;
import com.rebuild.backend.utils.NullSafeQuerySearchBuilder;
import jakarta.persistence.EntityManager;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class ElasticSearchService {

    private final EntityManager entityManager;

    private final RedisCacheManager cacheManager;

    public ElasticSearchService(EntityManager entityManager,
                                @Qualifier("searchCacheManager") RedisCacheManager cacheManager) {
        this.entityManager = entityManager;
        this.cacheManager = cacheManager;
    }

    public SearchResultDTO executeSearch(Object searchForm)
    {

        if (searchForm instanceof ResumeSpecsForm resumeSpecsForm)
        {
            return executeResumeSearch(resumeSpecsForm);
        }
        else if (searchForm instanceof ForumSpecsForm forumSpecsForm){
            return executePostSearch(forumSpecsForm);
        }
        return null;

    }

    @SuppressWarnings("unchecked")
    public SearchResultDTO getFromCache(String searchToken)
    {
        List<UUID> cacheResult = (List<UUID>) cacheManager.getCache("search_cache");

        if (cacheResult != null)
        {
            return new SearchResultDTO(cacheResult, searchToken);
        }

        else {
            return null;
        }

    }



    private SearchResultDTO executeResumeSearch(ResumeSpecsForm specsForm)
    {
        String newSearchToken = UUID.randomUUID().toString();
        SearchSession searchSession = Search.session(entityManager);
        List<UUID> matchedIds = searchSession.search(Resume.class)
                .select(f -> f.id(UUID.class))
                .where(f -> new NullSafeQuerySearchBuilder(f).
                        nullSafeMatch("header.firstName", specsForm.firstNameContains()).
                        nullSafeMatch("header.lastName", specsForm.lastNameContains()).
                        nullSafeMatch("name", specsForm.resumeNameContains()).
                        nullSafeMatch("education.schoolName", specsForm.schoolNameContains()).
                        nullSafeMatch("education.relevantCoursework", specsForm.courseWorkContains()).
                        nullSafeMatch("experiences.companyName", specsForm.companyContains()).
                        nullSafeMatch("experiences.technologyList", specsForm.technologyListContains()).
                        nullSafeMatch("experiences.bullets", specsForm.bulletsContains()).
                        nullSafeRangeMatch("creationTime", specsForm.creationAfterCutoff(), true).
                        nullSafeRangeMatch("creationTime", specsForm.creationBeforeCutoff(), false).
                        obtain()
                )
                .sort(f -> f.composite(
                        composite -> {
                        composite.add(f.field("creationTime").desc());
                        composite.add(f.field("lastModifiedTime").desc());
                        }
                        ))
                .fetchAllHits();
            Objects.requireNonNull(cacheManager.getCache("search_cache")).
                    put(newSearchToken, matchedIds);
            return new SearchResultDTO(matchedIds, newSearchToken);


    }

    private SearchResultDTO executePostSearch(ForumSpecsForm forumSpecsForm){
        SearchSession searchSession = Search.session(entityManager);
        List<UUID> matchedIds = searchSession.search(ForumPost.class)
                .select(f -> f.id(UUID.class))
                .where(f -> new NullSafeQuerySearchBuilder(f).
                        nullSafeMatch("title", forumSpecsForm.titleContains()).
                        nullSafeMatch("content", forumSpecsForm.bodyContains()).
                        nullSafeRangeMatch("creationDate", forumSpecsForm.postAfterCutoff(), true).
                        nullSafeRangeMatch("creationDate", forumSpecsForm.postBeforeCutoff(), false).
                        obtain()

                )
                .sort(f -> f.composite(
                            composite -> {
                            composite.add(f.field("creationDate").desc());
                            composite.add(f.field("lastModificationDate").desc());
                            }
                            ))
                .fetchAllHits();
            String searchResultToken = UUID.randomUUID().toString();
            Objects.requireNonNull(cacheManager.getCache("search_cache")).
                    put(searchResultToken, matchedIds);
            return new SearchResultDTO(matchedIds, searchResultToken);

    }

    public List<UUID> getNecessaryResults(List<UUID> allIds, int pageNumber, int pageSize)
    {
        if (allIds == null || allIds.isEmpty()) {
            return List.of();
        }

        int fromIndex = pageNumber * pageSize;
        if (fromIndex >= allIds.size()) {
            return List.of();
        }

        int toIndex = Math.min(fromIndex + pageSize, allIds.size());
        return allIds.subList(fromIndex, toIndex);
    }
}
