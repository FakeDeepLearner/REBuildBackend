package com.rebuild.backend.model.responses;

import com.rebuild.backend.model.forms.dtos.forum_dtos.UsernameSearchResultDTO;

import java.util.List;

public record UsernameSearchResponse(List<UsernameSearchResultDTO> dtoList) {
}
