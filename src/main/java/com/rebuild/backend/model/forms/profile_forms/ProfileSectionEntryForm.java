package com.rebuild.backend.model.forms.profile_forms;

import java.util.List;

public record ProfileSectionEntryForm(String title, List<String> toolsUsed,
                                      String location, String startTime, String endTime,
                                      List<String> bullets) {
}
