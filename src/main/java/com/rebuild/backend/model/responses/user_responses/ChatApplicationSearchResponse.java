package com.rebuild.backend.model.responses.user_responses;

import com.rebuild.backend.model.dtos.user_dtos.ChatApplicationDisplayDTO;

import java.util.List;

public record ChatApplicationSearchResponse(List<ChatApplicationDisplayDTO> applications,
                                            int currentPage, boolean hasNext) {
}
