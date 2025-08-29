package com.rebuild.backend.model.metamodel;

import com.rebuild.backend.model.entities.resume_entities.Section;
import com.rebuild.backend.model.entities.resume_entities.SectionEntry;
import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.ListAttribute;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.processor.HibernateProcessor")
@StaticMetamodel(Section.class)
public class ResumeSection_ {
    public static volatile ListAttribute<Section, SectionEntry> entries;
    public static volatile SingularAttribute<Section, String> title;

    public static volatile ListAttribute<Section, String> bullets;
}
