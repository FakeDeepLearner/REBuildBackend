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


    //TODO: Add support for months carrying over into another year
    public static <T> Specification<T> createDateComparisonSpecification(Expression<String> dataBaseDate,
                                                                         String inputDate,
                                                                         CompareCriteria criteria,
                                                                         ComparisonMethod method,
                                                                         int cutoff){

        return (root, query, criteriaBuilder) -> {

            Expression<Integer> cutoffLiteral = criteriaBuilder.literal(cutoff);

            String[] inputDateParts = inputDate.split("-");
            String inputYear = inputDateParts[1];
            String inputMonth = inputDateParts[0];

            int inputYearInt = Integer.parseInt(inputYear);
            int inputMonthInt = Integer.parseInt(inputMonth);

            Expression<String> monthExpr = criteriaBuilder.function("SUBSTRING", String.class,
                    dataBaseDate, criteriaBuilder.literal(1), criteriaBuilder.literal(2));
            Expression<String> yearExpr = criteriaBuilder.function("SUBSTRING", String.class,
                    dataBaseDate, criteriaBuilder.literal(4), criteriaBuilder.literal(4));

            Expression<Integer> databaseMonthExpression =
                    criteriaBuilder.function("CAST", Integer.class,
                            monthExpr);
            Expression<Integer> databaseYearExpression =
                    criteriaBuilder.function("CAST", Integer.class,
                            yearExpr);

            if(criteria == CompareCriteria.YEAR){
                if(method == ComparisonMethod.AFTER){
                    return criteriaBuilder.ge(databaseYearExpression, inputYearInt + cutoff);
                }
                else{
                    return criteriaBuilder.le(
                            criteriaBuilder.sum(databaseYearExpression, cutoffLiteral),
                            inputYearInt
                    );
                }
            }

            if(criteria == CompareCriteria.MONTH){
                if(method == ComparisonMethod.AFTER){
                    return criteriaBuilder.ge(databaseMonthExpression, inputMonthInt + cutoff);
                }
                else{
                    return criteriaBuilder.le(
                            criteriaBuilder.sum(databaseMonthExpression, cutoffLiteral),
                            inputMonthInt
                    );
                }
            }
            return null;
        };
    }

}
