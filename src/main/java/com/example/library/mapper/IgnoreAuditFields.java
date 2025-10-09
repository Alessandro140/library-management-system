package com.example.library.mapper;

import org.mapstruct.Mapping;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

// Use SOURCE retention for composed MapStruct annotations so they are
// visible to the annotation processor at compile time. Also restrict
// target to methods/types where mapping annotations are applied.
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD, ElementType.TYPE})
@Mapping(target = "created_at", ignore = true)
@Mapping(target = "updated_at", ignore = true)
public @interface IgnoreAuditFields {}