package com.maja.med_app.controller;

import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AppValidationException extends RuntimeException {
    
    private final Map<String, String> errors; 
}