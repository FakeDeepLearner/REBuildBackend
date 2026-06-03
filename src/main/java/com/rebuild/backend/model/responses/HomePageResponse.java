package com.rebuild.backend.model.responses;

import com.rebuild.backend.model.responses.resume_responses.HomeScreenResumeResponse;

import java.util.List;

public record HomePageResponse(List<HomeScreenResumeResponse> displayedResumes,
                               boolean hasNext) {
}
