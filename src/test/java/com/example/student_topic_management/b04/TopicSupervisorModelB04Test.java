package com.example.student_topic_management.b04;

import com.example.student_topic_management.dto.TopicSupervisorForm;
import com.example.student_topic_management.entity.TopicSupervisorId;
import jakarta.validation.*;
import java.io.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import static org.junit.jupiter.api.Assertions.*;

class TopicSupervisorModelB04Test {
    static ValidatorFactory factory;
    static Validator validator;
    @BeforeAll static void createValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    @AfterAll static void closeValidator() { factory.close(); }

    @ParameterizedTest @ValueSource(ints = {1, 2})
    void validFormAcceptsBothSlots(int slot) {
        TopicSupervisorForm form = new TopicSupervisorForm();
        form.setLecturerId(20L);
        form.setSlotNo(slot);
        assertTrue(validator.validate(form).isEmpty());
    }

    @ParameterizedTest @NullSource @ValueSource(longs = {-1L, 0L})
    void invalidLecturerIdIsRejected(Long id) {
        TopicSupervisorForm form = new TopicSupervisorForm();
        form.setLecturerId(id);
        form.setSlotNo(1);
        assertTrue(validator.validate(form).stream().anyMatch(error -> error.getPropertyPath().toString().equals("lecturerId")));
    }

    @ParameterizedTest @NullSource @ValueSource(ints = {-1, 0, 3, 256})
    void invalidSlotIsRejected(Integer slot) {
        TopicSupervisorForm form = new TopicSupervisorForm();
        form.setLecturerId(20L);
        form.setSlotNo(slot);
        assertTrue(validator.validate(form).stream().anyMatch(error -> error.getPropertyPath().toString().equals("slotNo")));
    }

    @Test void compositeIdUsesBothIdsAndSurvivesSerialization() throws Exception {
        TopicSupervisorId original = new TopicSupervisorId(10L, 20L);
        assertEquals(original, new TopicSupervisorId(10L, 20L));
        assertEquals(original.hashCode(), new TopicSupervisorId(10L, 20L).hashCode());
        assertNotEquals(original, new TopicSupervisorId(11L, 20L));
        assertNotEquals(original, new TopicSupervisorId(10L, 21L));
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream output = new ObjectOutputStream(bytes)) { output.writeObject(original); }
        try (ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            assertEquals(original, input.readObject());
        }
    }
}
