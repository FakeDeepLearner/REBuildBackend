package com.rebuild.backend.model.metamodel;

import com.rebuild.backend.model.entities.resume_entities.SectionEntry;
import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.ListAttribute;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

import java.time.YearMonth;

@Generated(value = "org.hibernate.processor.HibernateProcessor")
@StaticMetamodel(SectionEntry.class)
public class ResumeSectionEntry_ {

    public static volatile SingularAttribute<SectionEntry, String> title;
    public static volatile ListAttribute<SectionEntry, String> toolsUsed;
    
    public static volatile SingularAttribute<SectionEntry, String> location;
    public static volatile SingularAttribute<SectionEntry, YearMonth> startDate;
    public static volatile SingularAttribute<SectionEntry, YearMonth> endDate;
    public static volatile ListAttribute<SectionEntry, String> bullets;
}
