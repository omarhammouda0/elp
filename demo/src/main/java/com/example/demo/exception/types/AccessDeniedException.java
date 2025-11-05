package com.example.demo.exception.types;

import com.example.demo.exception.base.AppException;
import org.springframework.http.HttpStatus;




public class AccessDeniedException extends AppException {
    public AccessDeniedException(String code) {
        super( HttpStatus.FORBIDDEN, code, message);
    }
}