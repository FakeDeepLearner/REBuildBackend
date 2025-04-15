package com.rebuild.backend.model.metamodel;


import com.rebuild.backend.model.entities.resume_entities.Education;
import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.ListAttribute;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

import java.time.YearMonth;

@Generated(value = "org.hibernate.processor.HibernateProcessor")
@StaticMetamodel(Education_.class)
public class Education_ {
    public static volatile SingularAttribute<Education, String> schoolName;
    public static volatile ListAttribute<Education, String> relevantCoursework;
    public static volatile SingularAttribute<Education, YearMonth> startDate;
    public static volatile SingularAttribute<Education, YearMonth> endDate;
}
