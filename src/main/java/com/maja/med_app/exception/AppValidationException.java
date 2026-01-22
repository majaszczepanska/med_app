package com.maja.med_app.exception;

import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AppValidationException extends RuntimeException {
    
    private final Map<String, String> errors; 
}