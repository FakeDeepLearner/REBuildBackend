package com.rebuild.backend.model.responses;

import com.rebuild.backend.model.entities.profile_entities.UserProfile;
import com.rebuild.backend.model.entities.resume_entities.Resume;

import java.util.List;

public record HomePageData(List<Resume> displayedResumes, int currentPage,
                           long totalItems, int totalPages, int pageSize,
                           String searchToken) {
}
