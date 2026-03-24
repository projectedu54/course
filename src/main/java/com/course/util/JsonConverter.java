package com.course.util;

import com.course.exception.customException.BadRequestException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Converter
public class JsonConverter implements AttributeConverter<Map<String, Object>, String> {

    private static final Logger logger = LoggerFactory.getLogger(JsonConverter.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Map<String, Object> attribute) {
        // If the map is null or empty, store as NULL in DB
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new BadRequestException("Error converting JSON to String", "WRONG_FORMAT");
        }
    }

    @Override
    public Map<String, Object> convertToEntityAttribute(String dbData) {
        // FIX: Return null if the DB column is null, empty, or literally the string "null"
        if (dbData == null || dbData.isBlank() || dbData.equalsIgnoreCase("null")) {
            return null;
        }
        try {
            return objectMapper.readValue(dbData, Map.class);
        } catch (Exception e) {
            // Log the actual bad data so you can find it in the DB
            logger.error("Failed to convert DB string to JSON Map. Data: {}", dbData);
            // Returning null here prevents the GET API from crashing due to one bad row
            return null;
        }
    }
}