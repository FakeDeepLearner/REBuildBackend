package com.rebuild.backend.model.dtos;

import java.util.List;

public record RecoveryCodesDTO(List<String> hashedCodes, List<String> displayedCodes) {
}
