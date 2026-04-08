package com.rebuild.backend.model.responses.resume_responses;

import java.util.List;
import java.util.UUID;

public record ExperienceResponse(UUID id, String companyName, List<String> technologies,
                                 String location, String type, String startDate,
                                 String endDate, List<String> bullets) {
}
