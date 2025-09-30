package com.example.demo.exception.types;

import com.example.demo.exception.base.AppException;
import org.springframework.http.HttpStatus;


public class InvalidOperationException extends AppException {
    public InvalidOperationException(String code , String message) {
        super ( HttpStatus.CONFLICT , code , message );
    }
}