package com.rebuild.backend.specs.resume_filter_specs;

import com.rebuild.backend.model.entities.resume_entities.Education;
import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.specs.ReusableJoinSpecification;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class EducationSpecifications {

    public static Specification<Resume> schoolNameContains(String schoolName) {

        return new ReusableJoinSpecification<Resume>() {
            @Override
            public Predicate toPredicate(Root<Resume> root,
                                         CriteriaQuery<?> query,
                                         CriteriaBuilder criteriaBuilder) {
                Join<Resume, Education> join = joinSingular(root, "education");
                Path<String> dbName = join.get("schoolName");
                return criteriaBuilder.like(dbName, "%" + schoolName + "%");
            }
        };

    }

    public static Specification<Resume> courseWorkContains(String input) {
        return new ReusableJoinSpecification<Resume>() {
            @Override
            public Predicate toPredicate(Root<Resume> root,
                                         CriteriaQuery<?> query,
                                         CriteriaBuilder criteriaBuilder) {
                Join<Resume, Education> educationJoin = joinSingular(root, "education");
                Path<List<String>> courseWorkPath = educationJoin.get("courseWork");
                return criteriaBuilder.isMember(input, courseWorkPath);
            }
        };
    }
}
