package com.rebuild.backend.specs;

import jakarta.persistence.criteria.*;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.ListAttribute;
import jakarta.persistence.metamodel.SetAttribute;
import jakarta.persistence.metamodel.SingularAttribute;
import org.springframework.data.jpa.domain.Specification;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public interface ReusableJoinSpecification<T> extends Specification<T> {

    Map<String, Join<?, ?>> joins = new HashMap<>();

    JoinType defaultJoinType = JoinType.INNER;

    default <Z, X> Join<Z, X> checkJoin(From<?, Z> path, Attribute<Z, X> attribute, JoinType joinType) {

        Join<Z, X> foundJoin = (Join<Z, X>) joins.getOrDefault(attribute.getName(), null);

        //If we already have a join with this attribute, return it. Otherwise, create one and put it in our map
        if (foundJoin == null) {
            Join<Z, X> newJoin = path.join(attribute.getName(), joinType);
            joins.put(attribute.getName(), newJoin);
            return newJoin;
        }
        return foundJoin;
    }

    default <Z, X> ListJoin<Z, X> joinList(From<?, Z> from, ListAttribute<Z, X> attribute) {
        return (ListJoin<Z, X>) checkJoin(from, attribute, defaultJoinType); // Uses static constant
    }

    /**
     * Convenience method to join set attributes with reuse logic.
     */
    default <Z, X> SetJoin<Z, X> joinSet(From<?, Z> from, SetAttribute<Z, X> attribute) {
        return (SetJoin<Z, X>) checkJoin(from, attribute, defaultJoinType); // Uses static constant
    }

    /**
     * Convenience method to join singular attributes with reuse logic.
     */
    default <Z, X> Join<Z, X> joinSingular(From<?, Z> from, SingularAttribute<Z, X> attribute) {
        return checkJoin(from, attribute, defaultJoinType); // Uses static constant
    }

}
