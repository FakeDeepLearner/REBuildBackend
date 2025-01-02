package com.rebuild.backend.utils.specs.resume_filter_specs;

import com.rebuild.backend.model.entities.resume_entities.Experience;
import org.springframework.data.jpa.domain.Specification;


public class ExperienceSpecifications {

    public static Specification<Experience> companyNameContains(String input){
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(root.get("companyName"), "%"+input+"%");
    }

    public static Specification<Experience> technologyListHas(String tech){
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.isMember(tech, root.get("technologyList"));
    }

    //TODO: Write time based specifications after the date refactor

    public static Specification<Experience> startDateAfter(String inputDate){
        return (root, query, criteriaBuilder) ->{

        };
    }

}
