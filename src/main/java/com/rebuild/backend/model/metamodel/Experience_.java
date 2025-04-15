package com.rebuild.backend.model.metamodel;

import com.rebuild.backend.model.entities.resume_entities.Experience;
import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.ListAttribute;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

import java.time.YearMonth;

@Generated(value = "org.hibernate.processor.HibernateProcessor")
@StaticMetamodel(Experience.class)
public class Experience_ {
    public static volatile SingularAttribute<Experience, String> companyName;
    public static volatile ListAttribute<Experience, String> technologyList;
    public static volatile SingularAttribute<Experience, YearMonth> startDate;
    public static volatile SingularAttribute<Experience, YearMonth> endDate;
    public static volatile ListAttribute<Experience, String> bullets;
}
