package com.internship.tool.exception;

import jakarta.validation.ValidationException;

public class InputValidationException extends ValidationException {

    public InputValidationException(String message) {
        super(message);
    }
}
