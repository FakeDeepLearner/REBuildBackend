package com.rebuild.backend.service.util_services;

import com.rebuild.backend.model.forms.forum_forms.ForumSpecsForm;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class SearchService {

    private final EntityManager entityManager;

    public SearchService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<UUID> executeResumeSearch(String nameToSearch)
    {
        return List.of();

    }

    public List<UUID> executePostSearch(ForumSpecsForm specsForm){
        return List.of();
    }


    public List<UUID> executeUserSearch(String exampleName)
    {
        return List.of();
    }
}
