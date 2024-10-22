package com.twitter.XClone.api.model;

import java.util.HashMap;
import java.util.Map;

public class ValidationErrorResponse {
    private Map<String, String> errors = new HashMap<>();

    public void addError(String field, String message) {
        errors.put(field, message);
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, String> errors) {
        this.errors = errors;
    }
}
