package com.rebuild.backend.model.dtos;

import java.util.List;

public record PasswordFeedbackDTO(int score, List<String> suggestions, String warning) {
}
