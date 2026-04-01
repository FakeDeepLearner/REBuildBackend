package com.rebuild.backend.service.util_services;

import org.hibernate.search.engine.search.predicate.dsl.BooleanPredicateClausesStep;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;

public class NullSafeQuerySearchBuilder {

    private final SearchPredicateFactory searchPredicateFactory;

    private BooleanPredicateClausesStep<?, ?> booleanPredicateClausesStep;


    public NullSafeQuerySearchBuilder(SearchPredicateFactory searchPredicateFactory) {
        this.searchPredicateFactory = searchPredicateFactory;
        this.booleanPredicateClausesStep = searchPredicateFactory.bool();
    }

    public NullSafeQuerySearchBuilder nullSafeMatch(String attribute,
                                                    Object filterValue)
    {
        if (filterValue != null)
        {
            booleanPredicateClausesStep = booleanPredicateClausesStep.filter(searchPredicateFactory.match().
                    field(attribute).matching(filterValue));
        }
        return this;
    }

    public BooleanPredicateClausesStep<?, ?> getResult()
    {
        return booleanPredicateClausesStep;
    }
}
