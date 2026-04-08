package com.rebuild.backend.model.responses.resume_responses;

import java.util.List;
import java.util.UUID;

public record ProjectResponse(UUID id, String name, List<String> technologies, String startDate,
                              String endDate, List<String> bullets) {
}
