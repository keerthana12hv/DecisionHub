package com.decisionhub.service.interfaces.community;

import com.decisionhub.dto.request.community.CategoryRequest;
import com.decisionhub.dto.response.community.CategoryResponse;

import java.util.List;

public interface CategoryService {

    CategoryResponse createCategory(CategoryRequest request);

    List<CategoryResponse> getAllCategories();

    CategoryResponse updateCategory(Long categoryId, CategoryRequest request);

    void deleteCategory(Long categoryId);
}