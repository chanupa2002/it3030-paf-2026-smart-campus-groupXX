package com.uninode.smartcampus.modules.tickets.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PriorityConverter implements AttributeConverter<Priority, String> {

    @Override
    public String convertToDatabaseColumn(Priority attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public Priority convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        try {
            return Priority.valueOf(dbData.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
