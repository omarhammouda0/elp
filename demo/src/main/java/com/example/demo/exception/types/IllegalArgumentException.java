package com.example.demo.exception.types;

import com.example.demo.exception.base.AppException;
import org.springframework.http.HttpStatus;


public class IllegalArgumentException extends AppException {
    public IllegalArgumentException(String code, String message) {
        super( HttpStatus.BAD_REQUEST, code, message);
    }
}