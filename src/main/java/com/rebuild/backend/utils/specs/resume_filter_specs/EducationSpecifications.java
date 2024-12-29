package com.rebuild.backend.utils.specs.resume_filter_specs;

import com.rebuild.backend.model.entities.resume_entities.Education;

import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

public class EducationSpecifications {

    public static Specification<Education> schoolContains(String school) {
        return (root, query, builder) ->
                builder.like(root.get("schoolName"), "%" + school + "%");
    }

    public static Specification<Education> courseworkListHas(String course) {
        return (root, query, builder) ->
                builder.isMember(course, root.get("relevantCoursework"));
    }



}
