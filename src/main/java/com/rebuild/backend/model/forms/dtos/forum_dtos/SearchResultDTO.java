package com.rebuild.backend.model.forms.dtos.forum_dtos;

import java.util.List;
import java.util.UUID;

public record SearchResultDTO(List<UUID> results, String searchToken) {
}
