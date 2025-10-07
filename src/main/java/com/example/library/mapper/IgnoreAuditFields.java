package com.example.library.mapper;

import org.mapstruct.Mapping;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.CLASS)
@Mapping(target = "created_at", ignore = true)
@Mapping(target = "updated_at", ignore = true)
public @interface IgnoreAuditFields {}