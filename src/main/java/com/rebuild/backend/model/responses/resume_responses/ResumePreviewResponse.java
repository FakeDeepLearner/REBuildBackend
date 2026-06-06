package com.rebuild.backend.model.responses.resume_responses;

import java.util.UUID;

public record ResumePreviewResponse(UUID resumeId, String resumeName, String previewUrl) {
}
