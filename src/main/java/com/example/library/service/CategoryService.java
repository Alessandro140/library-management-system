package com.example.library.service;

import com.example.library.dto.CategoryDTO;
import com.example.library.entity.Category;
import com.example.library.utils.RepositoryException;
import com.example.library.mapper.CategoryMapper;
import com.example.library.repository.CategoryRepository;
import com.example.library.specification.SpecsNotDeleted;

import jakarta.validation.constraints.NotNull;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
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
    public @NonNull Optional<CategoryDTO> getCategoryById(@NonNull Long id) throws NullInputException{
        if (id == null){
            throw new NullInputException("id");
        }
        return this.categoryRepository.findById(id).map(this.categoryMapper::toDto);
    }

    /**
     * Returns all the categories.
     *
     * @param categorySpecification
     * @param pageable
     * @return the pages of category dto.
     */
    public @NonNull Page<CategoryDTO> getCategories(Specification<Category> categorySpecification, @NonNull Pageable pageable) throws NullInputException{
        if (pageable == null){
            throw new NullInputException("pageable");
        }
        Specification<Category> specCategory = SpecsNotDeleted.ensureNotDeleted(categorySpecification);

        return categoryRepository.findAll(specCategory, pageable).map(this.categoryMapper::toDto);
    }

    /**
     * Create a new category.
     *
     * @param categoryDTO
     * @return the created category.
     */
    @Transactional
    public @NonNull CategoryDTO createCategory(@NonNull CategoryDTO categoryDTO) throws NullInputException{
        if (categoryDTO == null){
            throw new NullInputException("categoryDTO");
        }
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
    public @NotNull CategoryDTO updateCategory(@NonNull Long id, @NonNull CategoryDTO categoryDTO) throws CategoryNotFoundException, NullInputException{

        if(id == null && categoryDTO == null){
            throw new NullInputException("id", "categoryDTO");
        } else if (id == null){
            throw new NullInputException("id");
        } else if (categoryDTO == null){
            throw new NullInputException("categoryDTO");
        }

        Category categoryToUpdate = this.categoryRepository.findById(id)
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
    public void softDeleteCategory(@NonNull Long id) throws CategoryNotFoundException, NullInputException{

        if(id == null){
            throw new NullInputException("id");
        }
        Category category = this.categoryRepository.findById(id)
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
    public void deleteCategory(@NonNull Long id) throws CategoryNotFoundException, NullInputException{
        if(id == null){
            throw new NullInputException("id");
        }
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
    public CategoryDTO restoreCategoryById(@NonNull Long id) throws CategoryNotFoundException, NullInputException{
        if(id == null){
            throw new NullInputException("id");
        }

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

    public static class NullInputException extends RepositoryException.BadRequest{

        public NullInputException(@NotNull String parameterName){
            super("This parameter should be NonNull: " + parameterName);
        }

        public NullInputException(@NotNull String parameterName1, @NotNull String parameterName2){
            super("This parameters should be NonNull: " + parameterName1 + ", " + parameterName2);
        }
    }
}
