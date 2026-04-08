package com.rebuild.backend.model.responses.resume_responses;

import java.util.List;

public record ResumeResponse(HeaderResponse header, EducationResponse education,
                             List<ExperienceResponse> experiences, List<ProjectResponse> projects) {
}
