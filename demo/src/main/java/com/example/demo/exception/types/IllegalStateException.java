package com.example.demo.exception.types;

import com.example.demo.exception.base.AppException;
import org.springframework.http.HttpStatus;



public class IllegalStateException extends AppException {
    public IllegalStateException(String code, String message) {
        super( HttpStatus.BAD_REQUEST, code, message);
    }
}
