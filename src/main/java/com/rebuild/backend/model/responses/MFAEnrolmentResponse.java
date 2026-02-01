package com.rebuild.backend.model.responses;

import java.util.List;

public record MFAEnrolmentResponse(String generatedQRCode, List<String> generatedRecoveryCodes){
}
