package com.rebuild.backend.model.metamodel;

import com.rebuild.backend.model.entities.resume_entities.ResumeSection;
import com.rebuild.backend.model.entities.resume_entities.ResumeSectionEntry;
import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.ListAttribute;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.processor.HibernateProcessor")
@StaticMetamodel(ResumeSection.class)
public class ResumeSection_ {
    public static volatile ListAttribute<ResumeSection, ResumeSectionEntry> entries;
    public static volatile SingularAttribute<ResumeSection, String> title;

    public static volatile ListAttribute<ResumeSection, String> bullets;
}
