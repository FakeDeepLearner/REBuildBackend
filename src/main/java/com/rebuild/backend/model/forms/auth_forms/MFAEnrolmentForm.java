package com.rebuild.backend.model.forms.auth_forms;

import java.util.List;

public record MFAEnrolmentForm(String qrCode, List<String> recoveryCodes,
                               String enteredOTP, boolean codesUnretrievableConfirmation) {
}
