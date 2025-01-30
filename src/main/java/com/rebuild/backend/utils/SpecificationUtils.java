package com.rebuild.backend.utils;

import com.rebuild.backend.model.entities.enums.CompareCriteria;
import com.rebuild.backend.model.entities.enums.ComparisonMethod;
import com.rebuild.backend.model.entities.resume_entities.*;
import com.rebuild.backend.specs.YearMonthStringOperations;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

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


    public static <T> Specification<T> createDateComparisonSpecification(String inputDate,
                                                                         String dataBaseDate,
                                                                         CompareCriteria criteria,
                                                                         ComparisonMethod method,
                                                                         int cutoff){

        return (root, query, criteriaBuilder) -> {
            int result = YearMonthStringOperations.compare(inputDate, dataBaseDate,
                    criteria, method);
            Expression<Integer> resultExpression = criteriaBuilder.literal(result);
            //Below, we use the ge and le methods

            //At least cutoff months or years ahead
            if(method == ComparisonMethod.AFTER){
                return criteriaBuilder.ge(resultExpression, cutoff);
            }
            //At least cutoff months or years behind
            else{
                return criteriaBuilder.le(criteriaBuilder.literal(-cutoff),
                        resultExpression);
            }
        };
    }

}
