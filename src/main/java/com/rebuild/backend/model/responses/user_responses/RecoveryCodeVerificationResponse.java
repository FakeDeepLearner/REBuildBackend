package com.rebuild.backend.model.responses.user_responses;

import java.util.List;

public record RecoveryCodeVerificationResponse(boolean codeIsCorrect, boolean userOutOfCodes,
                                               List<String> newCodes) {
}
