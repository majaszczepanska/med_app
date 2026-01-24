package com.maja.med_app.util;  

import java.util.HashMap;
import java.util.Map;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

public class ValidationErrorUtils {

    public static Map<String, String> mapErrors(BindingResult result) {
        Map<String, String> errors = new HashMap<>();
        if(result.hasErrors()){
            for (FieldError error : result.getFieldErrors()) {
                String fieldName = error.getField();
                String newMessage = error.getDefaultMessage();
                String currentMessage = errors.getOrDefault(fieldName, "");
                String separator = currentMessage.isEmpty() ? "" : ", \n";
                errors.put(fieldName, currentMessage + separator + newMessage);
            }
        }
        return errors;
    }
}
