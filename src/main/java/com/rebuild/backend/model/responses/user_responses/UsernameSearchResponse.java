package com.rebuild.backend.model.responses.user_responses;

import com.rebuild.backend.model.dtos.user_dtos.UsernameSearchResultDTO;

import java.util.List;

public record UsernameSearchResponse(List<UsernameSearchResultDTO> dtoList, int currentPage,
                                     boolean hasNext) {
}
