package com.rebuild.backend.model.metamodel;

import com.rebuild.backend.model.entities.resume_entities.*;
import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.ListAttribute;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

import java.time.LocalDateTime;
import java.util.UUID;

@Generated(value = "org.hibernate.processor.HibernateProcessor")
@StaticMetamodel(Resume.class)
public class Resume_ {
    public static volatile SingularAttribute<Resume, String> name;
    public static volatile SingularAttribute<Resume, Header> header;
    public static volatile SingularAttribute<Resume, Education> education;
    public static volatile ListAttribute<Resume, Experience> experiences;
    public static volatile ListAttribute<Resume, ResumeSection> sections;
    public static volatile SingularAttribute<Resume, LocalDateTime> creationTime;
    public static volatile SingularAttribute<Resume, LocalDateTime> lastModifiedTime;

}
