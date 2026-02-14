package com.rebuild.backend.service.util_services;

import com.rebuild.backend.model.entities.forum_entities.ForumPost;
import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.model.entities.resume_entities.search_entities.ResumeSearchConfiguration;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.forms.forum_forms.ForumSpecsForm;
import com.rebuild.backend.model.forms.resume_forms.ResumeSpecsForm;
import com.rebuild.backend.utils.elastic_utils.NullSafeQuerySearchBuilder;
import jakarta.persistence.EntityManager;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ElasticSearchService {

    private final EntityManager entityManager;

    public ElasticSearchService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<UUID> searchForResumes(Object searchForm, User user)
    {

        if (searchForm instanceof ResumeSpecsForm resumeSpecsForm)
        {
            return executeResumeSearch(resumeSpecsForm, user);
        }
        if(searchForm instanceof ResumeSearchConfiguration searchConfiguration)
        {
            return executeResumeSearch(searchConfiguration, user);
        }
        return null;
    }

    private List<UUID> executeResumeSearch(ResumeSpecsForm specsForm, User user)
    {
        SearchSession searchSession = Search.session(entityManager);
        List<UUID> matchedIds = searchSession.search(Resume.class)
                .select(f -> f.id(UUID.class))
                .where(f -> new NullSafeQuerySearchBuilder(f).
                        nullSafeMatch("userId", user.getId()).
                        nullSafeMatch("header.firstName", specsForm.firstNameContains()).
                        nullSafeMatch("header.lastName", specsForm.lastNameContains()).
                        nullSafeMatch("name", specsForm.resumeNameContains()).
                        nullSafeMatch("education.schoolName", specsForm.schoolNameContains()).
                        nullSafeMatch("education.relevantCoursework", specsForm.courseWorkContains()).
                        nullSafeMatch("experiences.companyName", specsForm.companyContains()).
                        nullSafeMatch("experiences.technologyList", specsForm.experienceTechnologyListContains()).
                        nullSafeMatch("experiences.bullets", specsForm.experienceBulletsContains()).
                        nullSafeMatch("projects.projectName", specsForm.projectNameContains()).
                        nullSafeMatch("projects.bullets", specsForm.projectBulletsContains()).
                        nullSafeMatch("projects.technologyList", specsForm.projectTechnologyListContains()).
                        atLeast("creationTime", Instant.parse(specsForm.creationAfterCutoff())).
                        atMost("creationTime", Instant.parse(specsForm.creationBeforeCutoff())).
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

    private List<UUID> executeResumeSearch(ResumeSearchConfiguration searchConfiguration, User user)
    {
        SearchSession searchSession = Search.session(entityManager);
        List<UUID> matchedIds = searchSession.search(Resume.class)
                .select(f -> f.id(UUID.class))
                .where(f -> new NullSafeQuerySearchBuilder(f).
                        nullSafeMatch("userId", user.getId()).
                        nullSafeMatch("header.firstName", searchConfiguration.getHeaderSearchProperties().getFirstNameSearch()).
                        nullSafeMatch("header.lastName", searchConfiguration.getHeaderSearchProperties().getLastNameSearch()).
                        nullSafeMatch("name", searchConfiguration.getResumeNameSearch()).
                        nullSafeMatch("education.schoolName", searchConfiguration.getEducationSearchProperties().getSchoolNameSearch()).
                        nullSafeMatch("education.relevantCoursework", searchConfiguration.getEducationSearchProperties().getCourseworkSearch()).
                        nullSafeMatch("experiences.companyName", searchConfiguration.getExperienceSearchProperties().getCompanySearch()).
                        nullSafeMatch("experiences.technologyList", searchConfiguration.getExperienceSearchProperties().getExperienceTechnologiesSearch()).
                        nullSafeMatch("experiences.bullets", searchConfiguration.getExperienceSearchProperties().getExperienceBulletsSearch()).
                        nullSafeMatch("projects.projectName", searchConfiguration.getProjectSearchProperties().getProjectNameSearch()).
                        nullSafeMatch("projects.bullets", searchConfiguration.getProjectSearchProperties().getProjectBulletsSearch()).
                        nullSafeMatch("projects.technologyList", searchConfiguration.getProjectSearchProperties().getProjectTechnologyListSearch()).
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

    public List<UUID> executePostSearch(ForumSpecsForm forumSpecsForm){
        SearchSession searchSession = Search.session(entityManager);
        List<UUID> matchedIds = searchSession.search(ForumPost.class)
                .select(f -> f.id(UUID.class))
                .where(f -> new NullSafeQuerySearchBuilder(f).
                        nullSafeMatch("title", forumSpecsForm.titleContains()).
                        nullSafeMatch("content", forumSpecsForm.bodyContains()).
                        atLeast("creationDate", Instant.parse(forumSpecsForm.postAfterCutoff())).
                        atMost("creationDate", Instant.parse(forumSpecsForm.postBeforeCutoff())).
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
