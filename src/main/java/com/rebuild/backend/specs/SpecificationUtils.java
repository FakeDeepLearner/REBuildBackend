package com.rebuild.backend.specs;

import com.rebuild.backend.model.entities.enums.CompareCriteria;
import com.rebuild.backend.model.entities.enums.ComparisonMethod;
import com.rebuild.backend.model.entities.resume_entities.*;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.YearMonth;

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


    private static Predicate createMonthAfterPredicate(Expression<Integer> databaseMonthExpression,
                                                       Expression<Integer> databaseYearExpression,
                                                       int inputYearInt,
                                                       int inputMonthInt,
                                                       int cutoff,
                                                       CriteriaBuilder criteriaBuilder) {
        //This is the lower limit, the dates in the database must be at or beyond this point
        YearMonth lowerLimit = YearMonth.of(inputYearInt, inputMonthInt).plusMonths(cutoff);
        int newYear = lowerLimit.getYear();
        int newMonth = lowerLimit.getMonthValue();
        Expression<Integer> newYearExpr = criteriaBuilder.literal(newYear);
        Expression<Integer> newMonthExpr = criteriaBuilder.literal(newMonth);
        return criteriaBuilder.or(
                /*
                 * The reason that we also have to check for equality here is that,
                 * if we don't, the query will return all the objects with their years being more
                 * OR their months being more, so if the years are not greater than the database year
                 * we still must check them for equality before checking for months being greater.
                 * Additionally, since ge also returns true on equality,
                 * we have to check this condition first to ensure
                 * that the month check happens if the years are equal
                 * */
                criteriaBuilder.and(
                        criteriaBuilder.equal(databaseYearExpression, newYearExpr),
                        //If the years are equal, the difference between the months must be >= cutoff
                        criteriaBuilder.ge(
                                criteriaBuilder.diff(databaseMonthExpression, newMonthExpr),
                                cutoff
                        )
                ),
                criteriaBuilder.ge(databaseYearExpression, newYearExpr)
        );

    }


    private static Predicate createMonthBeforePredicate(Expression<Integer> databaseMonthExpression,
                                                       Expression<Integer> databaseYearExpression,
                                                       int inputYearInt,
                                                       int inputMonthInt,
                                                       int cutoff,
                                                       CriteriaBuilder criteriaBuilder){

        //This is the upper limit, we are looking for dates that are at or before this point
        YearMonth upperLimit = YearMonth.of(inputYearInt, inputMonthInt).minusMonths(cutoff);
        int newYear = upperLimit.getYear();
        int newMonth = upperLimit.getMonthValue();
        Expression<Integer> newYearExpr = criteriaBuilder.literal(newYear);
        Expression<Integer> newMonthExpr = criteriaBuilder.literal(newMonth);
        return criteriaBuilder.or(
                //The exact same logic in the function above applies here
                criteriaBuilder.and(
                        criteriaBuilder.equal(newYearExpr, databaseYearExpression),
                        //The exact same logic also applies here, but in the opposite direction
                        criteriaBuilder.ge(
                                criteriaBuilder.diff(newMonthExpr, databaseMonthExpression),
                                cutoff
                        )
                ),
                criteriaBuilder.le(databaseYearExpression, newYearExpr)
        );
    }


    public static <T> Specification<T> createDateComparisonSpecification(Expression<String> dataBaseDate,
                                                                         String inputDate,
                                                                         CompareCriteria criteria,
                                                                         ComparisonMethod method,
                                                                         int cutoff){

        return (root, query, criteriaBuilder) -> {
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
                return criteriaBuilder.le(databaseYearExpression, inputYearInt - cutoff);
            }

            if(criteria == CompareCriteria.MONTH){
                if(method == ComparisonMethod.AFTER){
                    return createMonthAfterPredicate(databaseMonthExpression,
                            databaseYearExpression, inputYearInt,
                            inputMonthInt, cutoff, criteriaBuilder);
                }
                return createMonthBeforePredicate(databaseMonthExpression,
                        databaseYearExpression, inputYearInt,
                        inputMonthInt, cutoff, criteriaBuilder);
            }
            return null;
        };
    }

}
