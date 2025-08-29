package com.rebuild.backend.model.forms.profile_forms;

import com.rebuild.backend.model.forms.resume_forms.EducationForm;
import com.rebuild.backend.model.forms.resume_forms.ExperienceForm;
import com.rebuild.backend.model.forms.resume_forms.HeaderForm;
import com.rebuild.backend.model.forms.resume_forms.SectionForm;

import java.util.List;

public record FullProfileForm(HeaderForm headerForm,
                              EducationForm educationForm,
                              List<ExperienceForm> experienceForms,
                              List<SectionForm> sectionForms) {
}
