package com.decisionhub.service.impl.community;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.decisionhub.dto.request.community.CategoryRequest;
import com.decisionhub.dto.response.community.CategoryResponse;
import com.decisionhub.entity.community.Category;
import com.decisionhub.exception.ResourceAlreadyExistsException;
import com.decisionhub.exception.ResourceNotFoundException;
import com.decisionhub.repository.community.CategoryRepository;
import com.decisionhub.service.interfaces.community.CategoryService;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public CategoryResponse createCategory(CategoryRequest request) {

        if (categoryRepository.existsByName(request.name())) {
            throw new ResourceAlreadyExistsException("Category name already exists");
        }

        if (categoryRepository.existsBySlug(request.slug())) {
            throw new ResourceAlreadyExistsException("Category slug already exists");
        }

        Category category = new Category();
        category.setName(request.name());
        category.setSlug(request.slug());
        category.setIsActive(true);

        category = categoryRepository.save(category);

        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getIsActive()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {

        return categoryRepository.findAll()
                .stream()
                .filter(Category::getIsActive)
                .map(category -> new CategoryResponse(
                        category.getId(),
                        category.getName(),
                        category.getSlug(),
                        category.getIsActive()
                ))
                .toList();
    }

    @Override
    public CategoryResponse updateCategory(Long categoryId, CategoryRequest request) {

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found"));

        if (!category.getName().equals(request.name())
                && categoryRepository.existsByName(request.name())) {

            throw new ResourceAlreadyExistsException("Category name already exists");
        }

        if (!category.getSlug().equals(request.slug())
                && categoryRepository.existsBySlug(request.slug())) {

            throw new ResourceAlreadyExistsException("Category slug already exists");
        }

        category.setName(request.name());
        category.setSlug(request.slug());

        category = categoryRepository.save(category);

        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getIsActive()
        );
    }

    @Override
    public void deleteCategory(Long categoryId) {

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found"));

        category.setIsActive(false);

        categoryRepository.save(category);
    }
}