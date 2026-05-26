package com.rebuild.backend.utils;

import com.rebuild.backend.model.entities.util_entitites.base_entities.ExperienceBulletPoint;
import com.rebuild.backend.model.entities.util_entitites.base_entities.ProjectBulletPoint;
import com.rebuild.backend.model.entities.util_entitites.base_entities.base_resume_entities.AbstractExperience;
import com.rebuild.backend.model.entities.util_entitites.base_entities.base_resume_entities.AbstractProject;

import java.util.List;
import java.util.stream.Collectors;

public class BulletsUtil {

    public static List<ProjectBulletPoint> createProjectBullets(List<String> texts, AbstractProject associatedProject)
    {
        return texts.stream().map(
                text -> new ProjectBulletPoint(text, associatedProject)
        ).collect(Collectors.toList());
    }


    public static List<ExperienceBulletPoint> createExperienceBullets(List<String> texts,
                                                                      AbstractExperience abstractExperience)
    {
        return texts.stream().map(
                text -> new ExperienceBulletPoint(text, abstractExperience)
        ).collect(Collectors.toList());
    }


    public static List<ProjectBulletPoint> copyProjectBullets(List<ProjectBulletPoint> bulletPoints,
                                                              AbstractProject project)
    {
        return bulletPoints.stream().map(
                bullet -> new ProjectBulletPoint(bullet.getText(), project)
        ).collect(Collectors.toList());
    }

    public static List<ExperienceBulletPoint> copyExperienceBullets(List<ExperienceBulletPoint> bulletPoints,
                                                                    AbstractExperience experience)
    {
        return bulletPoints.stream().map(
                bullet -> new ExperienceBulletPoint(bullet.getText(), experience)
        ).collect(Collectors.toList());
    }
}
