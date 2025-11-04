package com.example.demo.auth;

import com.example.demo.user.Role;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = com.example.demo.auth.RoleValidator.class)

public @interface ValidRole {

    String message() default "Invalid role selected";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    Role[] allowedRoles() default {};
}