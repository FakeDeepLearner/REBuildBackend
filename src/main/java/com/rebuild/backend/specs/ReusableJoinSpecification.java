package com.rebuild.backend.specs;

import jakarta.persistence.criteria.*;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.ListAttribute;
import jakarta.persistence.metamodel.SetAttribute;
import jakarta.persistence.metamodel.SingularAttribute;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public interface ReusableJoinSpecification<T> extends Specification<T> {

    JoinType defaultJoinType = JoinType.INNER;

    default <Z, X> Join<Z, X> checkJoin(From<Z, ?> path, String attributeName, JoinType joinType,
    Map<String, Join<?, ?>> hashedJoins) {


        return (Join<Z, X>) hashedJoins.computeIfAbsent(attributeName,
                _ -> path.join(attributeName, joinType));
    }

    /**
     * convenience method to join list attributes
     */
    default <Z, X> ListJoin<Z, X> joinList(From<Z, ?> from, String attributeName,
                                           Map<String, Join<?, ?>> cache) {
        return (ListJoin<Z, X>) checkJoin(from, attributeName, defaultJoinType, cache); // Uses static constant
    }

    /**
     * Convenience method to join set attributes with reuse logic.
     */
    default <Z, X> SetJoin<Z, X> joinSet(From<Z, ?> from, String attributeName,
                                         Map<String, Join<?, ?>> cache) {
        return (SetJoin<Z, X>) checkJoin(from, attributeName, defaultJoinType, cache); // Uses static constant
    }

    /**
     * Convenience method to join singular attributes with reuse logic.
     */
    default <Z, X> Join<Z, X> joinSingular(From<Z, ?> from, String attributeName,
                                           Map<String, Join<?, ?>> cache) {
        return checkJoin(from, attributeName, defaultJoinType, cache); // Uses static constant
    }

}
