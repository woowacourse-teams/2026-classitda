package com.classitda.classes.application.instructor.daily;

import com.classitda.common.exception.ClassitdaException;
import com.classitda.common.exception.CommonErrorCode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.regex.Pattern;

record InstructorSessionCursor(LocalDateTime startAt, Long id) {

    private static final String DELIMITER = "|";

    static InstructorSessionCursor decode(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }

        try {
            String decoded = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            String[] parts = decoded.split(Pattern.quote(DELIMITER), -1);
            if (parts.length != 2) {
                throw invalidCursor();
            }

            LocalDateTime startAt = LocalDateTime.parse(parts[0]);
            long id = Long.parseLong(parts[1]);
            if (id < 1) {
                throw invalidCursor();
            }
            return new InstructorSessionCursor(startAt, id);
        } catch (IllegalArgumentException | DateTimeParseException exception) {
            throw invalidCursor();
        }
    }

    String encode() {
        String cursor = startAt + DELIMITER + id;
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(cursor.getBytes(StandardCharsets.UTF_8));
    }

    private static ClassitdaException invalidCursor() {
        return new ClassitdaException(CommonErrorCode.INVALID_INPUT);
    }
}
