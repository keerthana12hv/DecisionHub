package com.decisionhub.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.decisionhub.entity.community.Category;
import com.decisionhub.repository.community.CategoryRepository;

@Configuration
public class CategoryInitializer {

    @Bean
    CommandLineRunner initializeCategories(CategoryRepository categoryRepository) {
        return args -> {

            if (categoryRepository.count() > 0) {
                return;
            }

            createCategory(categoryRepository, "Technology", "technology");
            createCategory(categoryRepository, "Artificial Intelligence", "artificial-intelligence");
            createCategory(categoryRepository, "Programming", "programming");
            createCategory(categoryRepository, "Education", "education");
            createCategory(categoryRepository, "Career", "career");
            createCategory(categoryRepository, "Business", "business");
            createCategory(categoryRepository, "Finance", "finance");
            createCategory(categoryRepository, "Healthcare", "healthcare");
            createCategory(categoryRepository, "Science", "science");
            createCategory(categoryRepository, "Travel", "travel");
            createCategory(categoryRepository, "Food", "food");
            createCategory(categoryRepository, "Sports", "sports");
            createCategory(categoryRepository, "Entertainment", "entertainment");
            createCategory(categoryRepository, "Lifestyle", "lifestyle");
            createCategory(categoryRepository, "Environment", "environment");
            createCategory(categoryRepository, "Government & Politics", "government-politics");
            createCategory(categoryRepository, "Shopping", "shopping");
            createCategory(categoryRepository, "Gaming", "gaming");
            createCategory(categoryRepository, "Social", "social");
            createCategory(categoryRepository, "Others", "others");

            System.out.println("✅ Default categories initialized.");
        };
    }

    private void createCategory(CategoryRepository repository, String name, String slug) {

        Category category = new Category();
        category.setName(name);
        category.setSlug(slug);
        category.setIsActive(true);

        repository.save(category);
    }
}