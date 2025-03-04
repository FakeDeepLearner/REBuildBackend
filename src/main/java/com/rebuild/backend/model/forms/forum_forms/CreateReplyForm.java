package com.rebuild.backend.model.forms.forum_forms;

import java.util.UUID;

public record CreateReplyForm(UUID parent_reply_id, String content) {
}
