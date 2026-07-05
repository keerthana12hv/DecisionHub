package com.decisionhub.repository.community;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.decisionhub.entity.community.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findBySlug(String slug);

    Optional<Category> findByName(String name);

    boolean existsBySlug(String slug);

    boolean existsByName(String name);

}