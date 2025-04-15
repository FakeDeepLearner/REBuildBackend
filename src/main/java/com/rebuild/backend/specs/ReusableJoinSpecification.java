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

    //Declaring a variable in an interface makes it static.
    //This is usually not desirable, but we want it in this case,
    //because every instance of this interface will have the same map.
    Map<String, Join<?, ?>> joins = new HashMap<>();

    JoinType defaultJoinType = JoinType.INNER;

    default <Z, X> Join<Z, X> checkJoin(From<Z, ?> path, String attributeName, JoinType joinType) {

        Join<Z, X> foundJoin = (Join<Z, X>) joins.getOrDefault(attributeName, null);

        //If we already have a join with this attribute, return it. Otherwise, create one and put it in our map
        // (and also return it)
        if (foundJoin == null) {
            Join<Z, X> newJoin = path.join(attributeName, joinType);
            joins.put(attributeName, newJoin);
            return newJoin;
        }
        return foundJoin;
    }

    /**
     * convenience method to join list attributes
     */
    default <Z, X> ListJoin<Z, X> joinList(From<Z, ?> from, String attributeName) {
        return (ListJoin<Z, X>) checkJoin(from, attributeName, defaultJoinType); // Uses static constant
    }

    /**
     * Convenience method to join set attributes with reuse logic.
     */
    default <Z, X> SetJoin<Z, X> joinSet(From<Z, ?> from, String attributeName) {
        return (SetJoin<Z, X>) checkJoin(from, attributeName, defaultJoinType); // Uses static constant
    }

    /**
     * Convenience method to join singular attributes with reuse logic.
     */
    default <Z, X> Join<Z, X> joinSingular(From<Z, ?> from, String attributeName) {
        return checkJoin(from, attributeName, defaultJoinType); // Uses static constant
    }

}
