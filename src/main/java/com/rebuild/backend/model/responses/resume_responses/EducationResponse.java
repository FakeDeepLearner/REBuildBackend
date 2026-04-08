package com.rebuild.backend.model.responses.resume_responses;

import java.util.List;

public record EducationResponse(String schoolName, List<String> coursework, String location,
                                String startDate, String endDate) {
}
