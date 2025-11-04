package com.example.demo.exception.types;

import com.example.demo.exception.base.AppException;
import org.springframework.http.HttpStatus;

public class LastAdminException extends AppException {
    public LastAdminException(String code, String message) {
        super( HttpStatus.NOT_FOUND, code, message);
    }
}