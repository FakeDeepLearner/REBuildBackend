package com.rebuild.backend.model.forms;

import java.time.temporal.ChronoUnit;

public record AccountActivationForm(String email, Long timeCount, ChronoUnit timeUnit) {
}
