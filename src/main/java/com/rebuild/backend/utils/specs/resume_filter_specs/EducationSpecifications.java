package com.rebuild.backend.utils.specs.resume_filter_specs;

import com.rebuild.backend.model.entities.resume_entities.Education;
import com.rebuild.backend.model.entities.resume_entities.Resume;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

public class EducationSpecifications {

    public static Specification<Resume> schoolContains(String school) {
        return (root, query, builder) ->
                builder.like(root.get("education").get("schoolName"), "%" + school + "%");
    }

    public static Specification<Resume> courseworkContains(String course) {
        return (root, query, builder) ->
        {
            Join<Resume, Education> resumeEducationJoin = root.join("education");
            return builder.isMember(course, resumeEducationJoin.get("relevantCoursework"));
        };
    }
}
