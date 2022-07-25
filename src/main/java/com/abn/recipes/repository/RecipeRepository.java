package com.abn.recipes.repository;

import com.abn.recipes.entity.Recipe;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RecipeRepository  extends MongoRepository<Recipe, String> {
    Optional<Recipe> findByName(String name);
}
