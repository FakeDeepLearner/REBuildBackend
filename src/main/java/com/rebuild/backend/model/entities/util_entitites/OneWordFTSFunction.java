package com.rebuild.backend.model.entities.util_entitites;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.boot.model.FunctionContributor;
import org.hibernate.metamodel.model.domain.ReturnableType;
import org.hibernate.query.sqm.function.AbstractSqmSelfRenderingFunctionDescriptor;
import org.hibernate.query.sqm.function.FunctionKind;
import org.hibernate.query.sqm.produce.function.*;
import org.hibernate.sql.ast.SqlAstTranslator;
import org.hibernate.sql.ast.spi.SqlAppender;
import org.hibernate.sql.ast.tree.SqlAstNode;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.spi.TypeConfiguration;

import java.util.List;
import java.util.Objects;

public class OneWordFTSFunction extends AbstractSqmSelfRenderingFunctionDescriptor {

    public OneWordFTSFunction(TypeConfiguration configuration)
    {
        super(
                "one_word_fts",
                FunctionKind.NORMAL,
                StandardArgumentsValidators.exactly(2),
                StandardFunctionReturnTypeResolvers.invariant(
                        Objects.requireNonNull(configuration.getBasicTypeRegistry().
                                resolve(StandardBasicTypes.BOOLEAN))
                ),
                null


        );
    }

    @Override
    public void render(SqlAppender sqlAppender,
                       List<? extends SqlAstNode> sqlAstArguments,
                       ReturnableType<?> returnType, SqlAstTranslator<?> walker) {
        SqlAstNode text =  sqlAstArguments.getFirst();
        SqlAstNode query = sqlAstArguments.get(1);

        sqlAppender.appendSql("to_tsvector('english', ");
        text.accept(walker);
        sqlAppender.appendSql(")");


        sqlAppender.appendSql("@@ websearch_to_tsquery('english', ");
        query.accept(walker);
        sqlAppender.appendSql(")");
    }
}
