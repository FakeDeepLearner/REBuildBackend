package com.rebuild.backend.utils.database_utils;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.*;

import java.lang.reflect.Member;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.EnumSet;
import java.util.UUID;

public class UUIDV7IdGenerator implements BeforeExecutionGenerator, AnnotationBasedGenerator<GenerateV7UUID> {



    @Override
    public Object generate(SharedSessionContractImplementor sharedSessionContractImplementor,
                           Object o, Object o1, EventType eventType) {
        try(Connection obtainedConnection = sharedSessionContractImplementor.
                getJdbcCoordinator().
                getLogicalConnection().
                getPhysicalConnection(); Statement obtainedStatement = obtainedConnection.createStatement();
                ResultSet queryResult = obtainedStatement.executeQuery("SELECT pg_catalog.uuidv7();"))
        {
                queryResult.next();
                return UUID.fromString(queryResult.getString(1));
        }
        catch (SQLException ex)
        {
            throw new RuntimeException("Failed UUID generation");
        }

    }

    @Override
    public EnumSet<EventType> getEventTypes() {
        return EventTypeSets.INSERT_ONLY;
    }


    @Override
    public void initialize(GenerateV7UUID generateV7UUID,
                           Member member,
                           GeneratorCreationContext generatorCreationContext) {

    }
}
