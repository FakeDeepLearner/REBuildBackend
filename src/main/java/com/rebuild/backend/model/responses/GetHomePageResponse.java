package com.rebuild.backend.model.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rebuild.backend.model.entities.profile_entities.UserProfile;
import com.rebuild.backend.model.entities.resume_entities.Resume;

import java.util.List;

public record GetHomePageResponse(@JsonProperty("resumes") List<Resume> allResumes,
                                  @JsonProperty("profile") UserProfile profile) {
}
