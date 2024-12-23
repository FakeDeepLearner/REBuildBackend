package com.rebuild.backend.utils.specs.resume_filter_specs;

import com.rebuild.backend.model.entities.resume_entities.Education;
import com.rebuild.backend.model.entities.resume_entities.Resume;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class ResumeSpecifications {

    public static Specification<Resume> currentUserResumes(String email) {
        return (root, query, builder) ->
                builder.equal(root.get("user").get("email"), email);
    }

    public static Specification<Resume> lastEditedBefore(LocalDateTime date) {
        return (root, query, builder) ->
                builder.lessThanOrEqualTo(root.get("lastModifiedTime"), date);

    }

    public static Specification<Resume> createdBefore(LocalDateTime date) {
        return (root, query, builder) ->
                builder.lessThanOrEqualTo(root.get("creationTime"), date);

    }

    public static Specification<Resume> createdAfter(LocalDateTime date) {
        return (root, query, builder) ->
                builder.greaterThanOrEqualTo(root.get("creationTime"), date);

    }

    public static Specification<Resume> lastEditedAfter(LocalDateTime date) {
        return (root, query, builder) ->
                builder.greaterThanOrEqualTo(root.get("lastModifiedTime"), date);

    }

    public static Specification<Resume> nameContains(String name) {
        return (root, query, builder) ->
                builder.like(root.get("name"), "%" + name + "%");
    }




}
