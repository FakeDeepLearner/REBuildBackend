package com.rebuild.backend.model.responses;

import com.rebuild.backend.model.entities.resume_entities.Education;
import com.rebuild.backend.model.entities.resume_entities.Experience;
import com.rebuild.backend.model.entities.resume_entities.Header;

import java.util.List;

public record UserProfileResponse(Header header, Education education, List<Experience> experienceList) {
}
