package com.example.demo.exception.types;

import com.example.demo.exception.base.AppException;
import org.springframework.http.HttpStatus;

public class InActiveException extends AppException {
    public InActiveException(String code , String message) {
        super ( HttpStatus.FORBIDDEN , code , message );
    }
}
