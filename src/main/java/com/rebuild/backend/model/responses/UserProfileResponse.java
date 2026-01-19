package com.rebuild.backend.model.responses;

import com.rebuild.backend.model.entities.forum_entities.Comment;
import com.rebuild.backend.model.entities.resume_entities.Education;
import com.rebuild.backend.model.entities.resume_entities.Experience;
import com.rebuild.backend.model.entities.resume_entities.Header;
import com.rebuild.backend.model.entities.resume_entities.Project;

import java.util.List;

public record UserProfileResponse(Header header, Education education, List<Experience> experienceList,
                                  List<Project> projectsList, String profilePictureUrl, List<Comment> madeComments) {
}
