package com.example.library.service;

import com.example.library.dto.CategoryDTO;
import com.example.library.entity.Category;
import com.example.library.utils.RepositoryException;
import com.example.library.mapper.CategoryMapper;
import com.example.library.repository.CategoryRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.validation.constraints.NotNull;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


public class CategoryService {

    private final @NonNull CategoryRepository categoryRepository;
    private final @NonNull CategoryMapper categoryMapper;

    public CategoryService(@NonNull CategoryRepository categoryRepository, @NonNull CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    /**
     * Find a category by id.
     *
     * @param id
     * @return the optional of the category dto.
     */
    public @NonNull Optional<CategoryDTO> getCategoryById(@NonNull Long id){
        return this.categoryRepository.findByIdAndDeletedFalse(id).map(this.categoryMapper::toDto);
    }

    /**
     * Returns all the categories.
     *
     * @param categorySpecification
     * @param pageable
     * @return the pages of category dto.
     */
    public @NonNull Page<CategoryDTO> getCategories(Specification<Category> categorySpecification, Pageable pageable){
        Specification<Category> notDeletedSpec = (root, query, criteriaBuilder) ->
            criteriaBuilder.isFalse(root.get("deleted"));

        if (categorySpecification != null) {
            categorySpecification = categorySpecification.and(notDeletedSpec);
        } else {
            categorySpecification = notDeletedSpec;
        }

        return categoryRepository.findAll(categorySpecification, pageable).map(this.categoryMapper::toDto);
    }

    /**
     * Create a new category.
     *
     * @param categoryDTO
     * @return the created category.
     */
    @Transactional
    public @NonNull CategoryDTO createCategory(@NonNull CategoryDTO categoryDTO){

        categoryDTO.setId(null);

        Category categoryToSave = this.categoryMapper.toEntity(categoryDTO);
        Category categorySaved = this.categoryRepository.save(categoryToSave);

        return this.categoryMapper.toDto(categorySaved);
    }

    /**
     * Update a category.
     *
     * @param id
     * @param categoryDTO
     * @return the updated category
     * @throws CategoryNotFoundException
     */
    @Transactional
    public @NotNull CategoryDTO updateCategory(@NonNull Long id, @NonNull CategoryDTO categoryDTO) throws CategoryNotFoundException{

        Category categoryToUpdate = this.categoryRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));

        this.categoryMapper.updateCategory(categoryDTO, categoryToUpdate);

        return this.categoryMapper.toDto(categoryToUpdate);
    }

    /**
     * Soft delete a category.
     *
     * @param id
     * @throws CategoryNotFoundException
     */
    @Transactional
    public void softDeleteCategory(@NonNull Long id) throws CategoryNotFoundException{

        Category category = this.categoryRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() ->new CategoryNotFoundException(id));

        category.setDeleted(true);
    }

    /**
     * Delete a category.
     *
     * @param id
     * @throws CategoryNotFoundException
     */
    @Transactional
    public void deleteCategory(@NonNull Long id) throws CategoryNotFoundException{

        if (!this.categoryRepository.existsById(id)) {
            throw new CategoryNotFoundException(id);
        }
        this.categoryRepository.deleteById(id);
    }

    /**
     * Restore a soft deleted category by its id.
     *
     * @param id
     * @return
     * @throws CategoryNotFoundException
     */
    @Transactional
    public CategoryDTO restoreCategoryById(@NonNull Long id) throws CategoryNotFoundException{
        Category categoryToRestore = this.categoryRepository.findByIdAndDeletedTrue(id).orElseThrow(() -> new CategoryNotFoundException(id));

        categoryToRestore.setDeleted(false);

        return this.categoryMapper.toDto(categoryToRestore);
    }

    /**
     * Exception thrown when a category already exists.
     */
    public static class CategoryNotFoundException extends RepositoryException.NotFound {
        /**
         * Creates a new CategoryAlreadyExistsException with the given ISBN.
         *
         * @param isbn - the ISBN of the category
         */
        public CategoryNotFoundException(@NotNull Long id) {
            super("Category doesn't exist with id: " + id);
        }
    }
}
