package com.example.library.utils;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;

/**
 * A functional interface for composing specifications using logical operators.
 *
 * @param <T> the type of the entity
 */
public interface SpecificationComposer<T> extends Specification<T> {
    /**
     * Compose two specifications using the logical AND operator.
     *
     * @param spec1 the first specification
     * @param spec2 the second specification
     * @return a new specification that is the logical AND of the two input specifications
     */
    @Nullable
    static <T> Specification<T> and(@Nullable Specification<T> spec1, @Nullable Specification<T> spec2) {
        if (spec1 == null) {
            return spec2;
        } else if (spec2 == null) {
            return spec1;
        } else {
            return (root, query, cb) -> cb.and(spec1.toPredicate(root, query, cb), spec2.toPredicate(root, query, cb));
        }
    }

    /**
     * Compose two specifications using the logical OR operator.
     *
     * @param spec1 the first specification
     * @param spec2 the second specification
     * @return a new specification that is the logical OR of the two input specifications
     */
    @Nullable
    static <T> Specification<T> or(@Nullable Specification<T> spec1, @Nullable Specification<T> spec2) {
        if (spec1 == null) {
            return spec2;
        } else if (spec2 == null) {
            return spec1;
        } else {
            return (root, query, cb) -> cb.or(spec1.toPredicate(root, query, cb), spec2.toPredicate(root, query, cb));
        }
    }

    /**
     * Negate a specification using the logical NOT operator.
     *
     * @param spec the specification to negate
     * @return a new specification that is the logical NOT of the input specification
     */
    @Nullable
    static <T> Specification<T> not(@Nullable Specification<T> spec) {
        if (spec == null) {
            return null;
        } else {
            return (root, query, cb) -> cb.not(spec.toPredicate(root, query, cb));
        }
    }
}