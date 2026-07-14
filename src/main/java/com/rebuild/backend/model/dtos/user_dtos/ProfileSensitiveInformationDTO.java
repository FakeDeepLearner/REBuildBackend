package com.rebuild.backend.model.dtos.user_dtos;

public record ProfileSensitiveInformationDTO(String pictureUrl, String email, String forumUsername,
                                             String name, String phoneNumber) {
}
