package com.rebuild.backend.model.responses;

import java.util.List;

public record RecoveryCodeVerificationResponse(boolean codeIsCorrect, boolean userOutOfCodes,
                                               List<String> newCodes) {
}
