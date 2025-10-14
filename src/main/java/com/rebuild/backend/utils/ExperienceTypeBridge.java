package com.rebuild.backend.utils;

import com.rebuild.backend.model.entities.resume_entities.ExperienceType;
import org.hibernate.search.mapper.pojo.bridge.ValueBridge;
import org.hibernate.search.mapper.pojo.bridge.runtime.ValueBridgeFromIndexedValueContext;
import org.hibernate.search.mapper.pojo.bridge.runtime.ValueBridgeToIndexedValueContext;
import org.springframework.stereotype.Component;

@Component
public class ExperienceTypeBridge implements ValueBridge<ExperienceType, String> {
    @Override
    public String toIndexedValue(ExperienceType experienceType,
                                 ValueBridgeToIndexedValueContext valueBridgeToIndexedValueContext) {
        return experienceType.storedValue;
    }

    @Override
    public ExperienceType fromIndexedValue(String value, ValueBridgeFromIndexedValueContext context) {
        return ExperienceType.fromValue(value);
    }
}
