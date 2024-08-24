package com.rebuild.backend.model.responses;

import com.rebuild.backend.model.entities.resume_entities.Education;
import com.rebuild.backend.model.entities.resume_entities.Experience;
import com.rebuild.backend.model.entities.resume_entities.Header;

import java.util.List;
import java.util.UUID;

public record FullResumeUpdateResponse(UUID resume_id, Header newHeader, Education newEducation,
                                       List<Experience> newExperiences) {
}
