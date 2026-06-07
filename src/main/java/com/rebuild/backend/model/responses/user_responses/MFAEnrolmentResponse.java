package com.rebuild.backend.model.responses.user_responses;

import java.util.List;

public record MFAEnrolmentResponse(String generatedQRCode, List<String> generatedRecoveryCodes,
                                   String setupKey){
}
