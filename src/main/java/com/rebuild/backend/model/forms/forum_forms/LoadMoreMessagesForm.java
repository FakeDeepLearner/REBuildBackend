package com.rebuild.backend.model.forms.forum_forms;

public record LoadMoreMessagesForm(String lastTimestamp, boolean loadFromAbove) {
}
