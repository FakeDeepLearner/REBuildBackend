package com.rebuild.backend.model.forms.resume_forms;

import java.util.List;

public record ProjectForm(String projectName, List<String> technologyList,
                          String startDate, String endDate,
                          List<String> bullets) {
}
