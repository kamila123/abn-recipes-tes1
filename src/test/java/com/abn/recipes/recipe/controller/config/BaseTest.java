package com.abn.recipes.recipe.controller.config;

import com.abn.recipes.entity.Recipe;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public abstract class BaseTest {

    @BeforeAll
    public static void before() {
        RestAssured.baseURI = "http://localhost:8082/api";
    }

    public Recipe getRecipeEntity(String name, String instructions, String category, Integer servings, String[] ingredients) {
        return Recipe.builder()
                .name(name)
                .instructions(instructions)
                .category(category)
                .servings(servings)
                .ingredients(List.of(ingredients))
                .build();
    }
}
