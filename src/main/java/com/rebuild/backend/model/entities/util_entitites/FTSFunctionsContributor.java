package com.rebuild.backend.model.entities.util_entitites;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.boot.model.FunctionContributor;

public class FTSFunctionsContributor implements FunctionContributor {

    @Override
    public void contributeFunctions(FunctionContributions functionContributions) {
        functionContributions.getFunctionRegistry().register(
                "one_word_fts",
                new OneWordFTSFunction(functionContributions.getTypeConfiguration())
        );

    }
}
