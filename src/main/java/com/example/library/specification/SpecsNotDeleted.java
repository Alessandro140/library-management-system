package com.example.library.specification;

import org.springframework.data.jpa.domain.Specification;

import jakarta.annotation.Nullable;

/**
 * Define a specification to ignore the soft deleted element.
 */
public class SpecsNotDeleted {
    private SpecsNotDeleted() {}

    public static <T> Specification<T> notDeleted() {
        return (root, query, cb) -> cb.isFalse(root.get("deleted"));
    }

    public static <T> Specification<T> ensureNotDeleted(@Nullable Specification<T> spec) {
        Specification<T> notDeleted = notDeleted();
        return spec == null ? notDeleted : spec.and(notDeleted);
    }
}
