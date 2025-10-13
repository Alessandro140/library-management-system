package com.example.library.mapper;

import org.mapstruct.Mapping;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD, ElementType.TYPE})
@Mapping(target = "createdAt", ignore = true)
@Mapping(target = "updatedAt", ignore = true)
public @interface IgnoreAuditFields {}