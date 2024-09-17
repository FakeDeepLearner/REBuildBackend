package com.rebuild.backend.model.forms.profile_forms;

import java.time.temporal.Temporal;
import java.util.List;

public record ProfileExperienceForm(String companyName,
                                    List<String> technologies,
                                    Temporal startDate,
                                    Temporal endDate,
                                    List<String> bullets) {
}
