package com.rebuild.backend.model.responses;

import com.rebuild.backend.model.responses.resume_responses.ResumePreviewResponse;

import java.util.List;

public record HomePageResponse(List<ResumePreviewResponse> displayedResumes,
                               boolean hasNext) {
}
