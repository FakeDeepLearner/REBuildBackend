package com.rebuild.backend.specs;

import com.rebuild.backend.model.entities.forum_entities.ForumPost;
import com.rebuild.backend.model.forms.dtos.forum_dtos.ForumSpecsDTO;
import org.springframework.data.jpa.domain.Specification;

@FunctionalInterface
public interface SpecificationBuilder {

    Specification<ForumPost> buildSpecification(ForumSpecsDTO specsDTO);
}
