package com.rebuild.backend.utils;

import com.rebuild.backend.model.entities.resume_entities.*;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;

import java.util.List;

public class SpecificationUtils {

    //In essence, this means that the root must be a Resume,
    // and we must be going from a resume to one of its composed types
    private static <Z extends Resume, X extends ResumeProperty>  Join<Z, X> getPreComputedJoin(Root<Z> root,
                                                                                                                           JoinType joinType,
                                                                                                                           String propertyName) {
        return root.join(propertyName, joinType);
    }

    public static Join<Resume, Education> getPreComputedEducationJoin(Root<Resume> root,
                                                                        JoinType type,
                                                                        String propertyName) {
        return getPreComputedJoin(root, type, propertyName);
    }

    public static Join<Resume, Header> getPreComputedHeaderJoin(Root<Resume> root,
                                                                JoinType type,
                                                                String propertyName) {
        return getPreComputedJoin(root, type, propertyName);
    }

    public static Join<Resume, Experience> getPreComputedExperienceJoin(Root<Resume> root,
                                                                      JoinType type,
                                                                      String propertyName) {
        return getPreComputedJoin(root, type, propertyName);
    }

    public static Join<Resume, ResumeSection> getPreComputedSectionJoin(Root<Resume> root,
                                                                      JoinType type,
                                                                      String propertyName) {
        return getPreComputedJoin(root, type, propertyName);
    }

}
