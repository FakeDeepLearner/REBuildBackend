package com.rebuild.backend.model.metamodel;

import com.rebuild.backend.model.entities.resume_entities.ResumeSectionEntry;
import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.ListAttribute;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.processor.HibernateProcessor")
@StaticMetamodel(ResumeSectionEntry.class)
public class ResumeSectionEntry_ {

    public static volatile SingularAttribute<ResumeSectionEntry, String> title;
    public static volatile ListAttribute<ResumeSectionEntry, String> toolsUsed;
    
    public static volatile SingularAttribute<ResumeSectionEntry, String> location;

    public static volatile ListAttribute<ResumeSectionEntry, String> bullets;
}
