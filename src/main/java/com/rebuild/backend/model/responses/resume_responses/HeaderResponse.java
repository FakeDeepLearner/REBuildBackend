package com.rebuild.backend.model.responses.resume_responses;

import java.util.List;

public record HeaderResponse(String number, String name, String email, List<String> links) {
}
