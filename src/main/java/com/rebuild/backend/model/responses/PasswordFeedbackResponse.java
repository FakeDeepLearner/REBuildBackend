package com.rebuild.backend.model.responses;

import com.nulabinc.zxcvbn.Strength;

import java.util.List;

public record PasswordFeedbackResponse(int score, List<String> suggestions, String warning) {
}
