package com.rebuild.backend.service.resume_services;

import com.rebuild.backend.model.entities.resume_entities.*;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResumeSpecService {

    private <T> Specification<Resume> filterSpecificationList(List<Specification<T>> specifications) {
        String attributeToJoin = "";
        return (root, query, builder) -> {
            Join<Resume, T> join = root.join(attributeToJoin);
            return specifications.stream().
                    //TODO: This cast is a bit weird, fix it if possible
                    map(tSpecification -> tSpecification.toPredicate((Root<T>) join, query, builder)).
                    reduce(builder::and).orElse(null);
        };
    }

    public Specification<Resume> filterAllSpecs(List<Specification<Header>> headerSpecs,
                                                          List<Specification<Experience>> experienceSpecs,
                                                          List<Specification<ResumeSection>> sectionSpecs,
                                                          List<Specification<Education>> educationSpecs,
                                                          List<Specification<Resume>> mainSpecs) {
        Specification<Resume> combinedHeaderSpecs = filterSpecificationList(headerSpecs);
        Specification<Resume> combinedExperienceSpecs = filterSpecificationList(experienceSpecs);
        Specification<Resume> combinedSectionSpecs = filterSpecificationList(sectionSpecs);
        Specification<Resume> combinedEducationSpecs = filterSpecificationList(educationSpecs);
        Specification<Resume> fullCombinedSpecs =
                combinedHeaderSpecs.and(combinedExperienceSpecs).
                        and(combinedSectionSpecs).and(combinedEducationSpecs);
        /*
        Using the combined specifications derived above,
         apply them one by one to the main resume specifications that are given,
         using the "and" associative function in order to ensure all specifications are accounted for
        */
        return mainSpecs.stream().reduce(fullCombinedSpecs, Specification::and);


    }
}
