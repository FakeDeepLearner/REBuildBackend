package com.rebuild.backend.model.responses.resume_responses;

import java.util.UUID;

public record HomeScreenResumeResponse(UUID resumeId, String resumeName, String previewUrl) {
}
