package com.rebuild.backend.model.responses;

import com.rebuild.backend.model.dtos.ProfileHistoryCommentDTO;
import com.rebuild.backend.model.dtos.ProfileHistoryPostDTO;
import com.rebuild.backend.model.dtos.forum_dtos.ProfileSensitiveInformationDTO;
import com.rebuild.backend.model.entities.forum_entities.Comment;
import com.rebuild.backend.model.entities.forum_entities.ForumPost;

import java.util.List;

public record UserProfileResponse(ProfileSensitiveInformationDTO sensitiveInformationDTO,
                                  String forumUsername,
                                  List<ProfileHistoryCommentDTO> madeComments,
                                  List<ProfileHistoryPostDTO> madePosts) {
}
