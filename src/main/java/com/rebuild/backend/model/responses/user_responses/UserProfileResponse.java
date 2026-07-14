package com.rebuild.backend.model.responses.user_responses;

import com.rebuild.backend.model.dtos.user_dtos.ProfileHistoryCommentDTO;
import com.rebuild.backend.model.dtos.user_dtos.ProfileHistoryPostDTO;
import com.rebuild.backend.model.dtos.user_dtos.ProfileSensitiveInformationDTO;

import java.util.List;

public record UserProfileResponse(ProfileSensitiveInformationDTO sensitiveInformationDTO,
                                  List<ProfileHistoryCommentDTO> madeComments,
                                  List<ProfileHistoryPostDTO> madePosts) {
}
