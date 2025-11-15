package com.rebuild.backend.utils.elastic_utils;

import org.hibernate.search.engine.search.predicate.dsl.BooleanPredicateClausesStep;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;

public class NullSafeQuerySearchBuilder {

    private final SearchPredicateFactory searchPredicateFactory;

    private final BooleanPredicateClausesStep<?, ?> booleanPredicateClausesStep;


    public NullSafeQuerySearchBuilder(SearchPredicateFactory searchPredicateFactory) {
        this.searchPredicateFactory = searchPredicateFactory;
        this.booleanPredicateClausesStep = searchPredicateFactory.bool();
    }

    public NullSafeQuerySearchBuilder nullSafeMatch(String attribute,
                                                    Object filterValue)
    {
        if (filterValue != null)
        {
            booleanPredicateClausesStep.filter(searchPredicateFactory.match().
                    field(attribute).matching(filterValue));
        }
        return this;
    }

    public NullSafeQuerySearchBuilder nullSafeRangeMatch(String attribute,
                                                         Object filterValue, boolean atLeast)
    {
        if (filterValue != null)
        {
            if (atLeast) {
                booleanPredicateClausesStep.filter(searchPredicateFactory.range().
                        field(attribute).atLeast(filterValue));
            }
            else{
                booleanPredicateClausesStep.filter(searchPredicateFactory.range().
                        field(attribute).atMost(filterValue));
            }
        }
        return this;
    }

    public BooleanPredicateClausesStep<?, ?> obtain()
    {
        return booleanPredicateClausesStep;
    }
}
