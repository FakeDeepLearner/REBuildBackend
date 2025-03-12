package com.rebuild.backend.specs.resume_filter_specs;

import com.rebuild.backend.model.entities.resume_entities.Education;
import com.rebuild.backend.model.entities.resume_entities.Resume;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Path;
import org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor;
import org.springframework.data.jpa.domain.Specification;

public class EducationSpecifications {

    public Specification<Resume> schoolNameStartsWith(String schoolName,
                                                      Join<Resume, Education> preComputedJoin) {

        return (root, query, criteriaBuilder) ->
        {
            Path<String> dbName = preComputedJoin.get("schoolName");
            return criteriaBuilder.like(dbName, schoolName + "%");

        };

    }

    public Specification<Resume> courseWorkContains(String input,
                                                    Join<Resume, Education> preComputedJoin) {
        return (root, query, criteriaBuilder) ->
        {
            Path<String> courseWorkJoin = preComputedJoin.join("relevantCoursework");
            return criteriaBuilder.like(courseWorkJoin, input + "%");
        };
    }

    public Specification<Resume> startMonthAfterByValue(String input,
                                                Join<Resume, Education> preComputedJoin, int cutoff) {
        return (root, query, criteriaBuilder) ->
                null;
    }
}
