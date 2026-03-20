package com.rebuild.backend.model.responses;

import com.rebuild.backend.model.dtos.forum_dtos.ProfileSensitiveInformationDTO;
import com.rebuild.backend.model.entities.forum_entities.Comment;
import com.rebuild.backend.model.entities.forum_entities.ForumPost;
import com.rebuild.backend.model.entities.resume_entities.Education;
import com.rebuild.backend.model.entities.resume_entities.Experience;
import com.rebuild.backend.model.entities.resume_entities.Header;
import com.rebuild.backend.model.entities.resume_entities.Project;

import java.util.List;

public record UserProfileResponse(ProfileSensitiveInformationDTO sensitiveInformationDTO,
                                  String forumUsername,
                                  List<Comment> madeComments,
                                  List<ForumPost> madePosts) {
}
