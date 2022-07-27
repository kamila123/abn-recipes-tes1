package com.abn.recipes.recipe.controller;

import com.abn.recipes.entity.Recipe;
import com.abn.recipes.repository.RecipeRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.apache.http.HttpHeaders;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class RecipeControllerTest {

    @Autowired
    private RecipeRepository recipeRepository;

    private static final String API_PATH = "/v1/recipe/";

    @BeforeEach
    public void clean() {
        recipeRepository.deleteAll();
    }

    @BeforeAll
    public static void before() {
        RestAssured.baseURI = "http://localhost:8082/api";
    }

    @Test
    void create() {
        String[] chiliIngredients = { "100g chorizo , sliced", "400g can kidney beans" };
        var recipeDTO = getRecipeEntity(                "Quick chilli",
                "instructions Quick chilli", "VEGETARIAN", 2,chiliIngredients);

        given()
                .body(recipeDTO)
                .header(HttpHeaders.CONTENT_TYPE, ContentType.JSON)
                .when()
                .post(API_PATH)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("id", Matchers.notNullValue())
                .body("name", Matchers.is(recipeDTO.getName()))
                .body("instructions", Matchers.is(recipeDTO.getInstructions()))
                .body("servings", Matchers.is(recipeDTO.getServings()))
                .body("ingredients", Matchers.is(recipeDTO.getIngredients()));

    }

    @Test
    void createDuplicatedRecipe() {
        String[] chiliIngredients = { "100g chorizo , sliced", "400g can kidney beans" };
        var recipeDTO = getRecipeEntity(                "Quick chilli",
                "instructions Quick chilli", "VEGETARIAN", 2,chiliIngredients);

        given()
                .body(recipeDTO)
                .header(HttpHeaders.CONTENT_TYPE, ContentType.JSON)
                .when()
                .post(API_PATH)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("id", Matchers.notNullValue())
                .body("name", Matchers.is(recipeDTO.getName()))
                .body("instructions", Matchers.is(recipeDTO.getInstructions()))
                .body("servings", Matchers.is(recipeDTO.getServings()))
                .body("ingredients", Matchers.is(recipeDTO.getIngredients()));

        given()
                .body(recipeDTO)
                .header(HttpHeaders.CONTENT_TYPE, ContentType.JSON)
                .when()
                .post(API_PATH)
                .then()
                .statusCode(HttpStatus.CONFLICT.value());
    }

    @Test
    void update() {
        String[] chiliIngredients = { "100g chorizo , sliced", "400g can kidney beans" };
        var recipeEntity = getRecipeEntity(                "Quick chilli",
                "instructions Quick chilli", "VEGETARIAN", 2,chiliIngredients);

        var recipeSaved = recipeRepository.save(recipeEntity);

        var recipeDTO = getRecipeEntity(                "Quick chilli",
                "instructions Quick chilli", "LOW_CARB", 2,chiliIngredients);

        given()
                .body(recipeDTO)
                .header(HttpHeaders.CONTENT_TYPE, ContentType.JSON)
                .when()
                .put(API_PATH + recipeSaved.getId())
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("id", Matchers.notNullValue())
                .body("name", Matchers.is(recipeDTO.getName()))
                .body("instructions", Matchers.is(recipeDTO.getInstructions()))
                .body("servings", Matchers.is(recipeDTO.getServings()))
                .body("ingredients", Matchers.is(recipeDTO.getIngredients()));

    }

    @Test
    void updateNonExistingId() {
        String[] chiliIngredients = { "100g chorizo , sliced", "400g can kidney beans" };

        var recipeDTO = getRecipeEntity(                "Quick chilli",
                "instructions Quick chilli", "VEGETARIAN", 2,chiliIngredients);

        given()
                .body(recipeDTO)
                .header(HttpHeaders.CONTENT_TYPE, ContentType.JSON)
                .when()
                .put(API_PATH + "xpto")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());

    }

    @Test
    void filterConditionsNoFilter() {
        String[] saladIngredients = { "100g couscous", "2 spring onions", "100ml hot low salt vegetable stock (from a cube is fine)" };
        var recipeSaladDTO = getRecipeEntity("10-minute couscous salad",
                "instructions 10-minute couscous salad", "LOW_CARB", 2,saladIngredients);

        String[] chiliIngredients = { "100g chorizo , sliced", "400g can kidney beans" };
        var recipeChiliDTO = getRecipeEntity("Quick chilli",
                "instructions Quick chilli", "VEGETARIAN", 2,chiliIngredients);

        recipeRepository.saveAll(List.of(recipeSaladDTO, recipeChiliDTO));

        given()
                .header(HttpHeaders.CONTENT_TYPE, ContentType.JSON)
                .when()
                .get(API_PATH)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(2));

    }

    @Test
    void delete() {
        String[] saladIngredients = { "100g couscous", "2 spring onions", "100ml hot low salt vegetable stock (from a cube is fine)" };
        var recipeSaladDTO = getRecipeEntity("10-minute couscous salad",
                "instructions 10-minute couscous salad", "VEGETARIAN", 2,saladIngredients);

        var recipeSaved = recipeRepository.save(recipeSaladDTO);

        given()
                .header(HttpHeaders.CONTENT_TYPE, ContentType.JSON)
                .when()
                .delete(API_PATH + recipeSaved.getId())
                .then()
                .statusCode(HttpStatus.OK.value());

        var allRecipes = recipeRepository.findAll();
        assertThat(allRecipes.isEmpty()).isTrue();
    }

    @Test
    void deleteInvalidRecipe() {
       given()
                .header(HttpHeaders.CONTENT_TYPE, ContentType.JSON)
                .when()
                .delete(API_PATH + "xpto")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void filterConditionName() {
        String[] saladIngredients = { "100g couscous", "2 spring onions", "100ml hot low salt vegetable stock (from a cube is fine)" };
        var recipeSaladDTO = getRecipeEntity("10-minute couscous salad",
                "instructions 10-minute couscous salad", "VEGETARIAN", 2,saladIngredients);

        String[] chiliIngredients = { "100g chorizo , sliced", "400g can kidney beans" };
        var recipeChiliDTO = getRecipeEntity("Quick chilli",
                "instructions Quick chilli", "LOW_CARB", 2,chiliIngredients);

        recipeRepository.saveAll(List.of(recipeChiliDTO, recipeSaladDTO));

        given()
                .header(HttpHeaders.CONTENT_TYPE, ContentType.JSON)
                .param("name","Quick chilli")
                .when()
                .get(API_PATH)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(1))
                .body("" , hasItems(hasEntry("name", "Quick chilli")));

    }

    @Test
    void filterConditionCategory() {
        String[] saladIngredients = { "100g couscous", "2 spring onions", "100ml hot low salt vegetable stock (from a cube is fine)" };
        var recipeSaladDTO = getRecipeEntity("10-minute couscous salad",
                "instructions 10-minute couscous salad", "VEGETARIAN", 2,saladIngredients);

        String[] chiliIngredients = { "100g chorizo , sliced", "400g can kidney beans" };
        var recipeChiliDTO = getRecipeEntity("Quick chilli",
                "instructions Quick chilli", "LOW_CARB", 2,chiliIngredients);

        recipeRepository.saveAll(List.of(recipeChiliDTO, recipeSaladDTO));

         given()
                .header(HttpHeaders.CONTENT_TYPE, ContentType.JSON)
                 .param("category", "VEGETARIAN")
                .when()
                .get( API_PATH)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(1))
                .body("" , hasItems(hasEntry("category", "VEGETARIAN")));

    }

    @Test
    void filterConditionCategoryLowerCase() {
        String[] saladIngredients = { "100g couscous", "2 spring onions", "100ml hot low salt vegetable stock (from a cube is fine)" };
        var recipeSaladDTO = getRecipeEntity("10-minute couscous salad",
                "instructions 10-minute couscous salad", "VEGETARIAN", 2,saladIngredients);

        String[] chiliIngredients = { "100g chorizo , sliced", "400g can kidney beans" };
        var recipeChiliDTO = getRecipeEntity("Quick chilli",
                "instructions Quick chilli", "LOW_CARB", 2,chiliIngredients);

        recipeRepository.saveAll(List.of(recipeChiliDTO, recipeSaladDTO));

        given()
                .header(HttpHeaders.CONTENT_TYPE, ContentType.JSON)
                .param("category","low_carb")
                .when()
                .get(API_PATH)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(1))
                .body("" , hasItems(hasEntry("category", "LOW_CARB")));
    }

    @Test
    void filterConditionInstructions() {
        String[] saladIngredients = { "100g couscous", "2 spring onions", "100ml hot low salt vegetable stock (from a cube is fine)" };
        var recipeSaladDTO = getRecipeEntity("10-minute couscous salad",
                "instructions 10-minute couscous salad", "VEGETARIAN", 2,saladIngredients);

        String[] chiliIngredients = { "100g chorizo , sliced", "400g can kidney beans" };
        var recipeChiliDTO = getRecipeEntity("Quick chilli",
                "instructions Quick chilli", "LOW_CARB", 2,chiliIngredients);

        recipeRepository.saveAll(List.of(recipeChiliDTO, recipeSaladDTO));

        given()
                .header(HttpHeaders.CONTENT_TYPE, ContentType.JSON)
                .param("instructions", "couscous")
                .when()
                .get(API_PATH)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(1))
                .body("" , hasItems(hasEntry("instructions", "instructions 10-minute couscous salad")));
    }

    @Test
    void filterConditionServings() {
        String[] saladIngredients = { "100g couscous", "2 spring onions", "100ml hot low salt vegetable stock (from a cube is fine)" };
        var recipeSaladDTO = getRecipeEntity("10-minute couscous salad",
                "instructions 10-minute couscous salad", "VEGETARIAN", 2,saladIngredients);

        String[] chiliIngredients = { "100g chorizo , sliced", "400g can kidney beans" };
        var recipeChiliDTO = getRecipeEntity("Quick chilli",
                "instructions Quick chilli", "LOW_CARB", 6,chiliIngredients);

        recipeRepository.saveAll(List.of(recipeChiliDTO, recipeSaladDTO));

        given()
                .header(HttpHeaders.CONTENT_TYPE, ContentType.JSON)
                .param("servings", "2")
                .when()
                .get(API_PATH )
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(1))
                .body("" , hasItems(hasEntry("servings", 2)));

    }

    @Test
    void filterConditionIngredients() {
        String[] saladIngredients = { "100g couscous", "2 spring onions", "100ml hot low salt vegetable stock (from a cube is fine)" };
        var recipeSaladDTO = getRecipeEntity("10-minute couscous salad",
                "instructions 10-minute couscous salad", "VEGETARIAN", 2,saladIngredients);

        String[] chiliIngredients = { "100g chorizo , sliced", "400g can kidney beans" };
        var recipeChiliDTO = getRecipeEntity("Quick chilli",
                "instructions Quick chilli", "LOW_CARB", 6,chiliIngredients);

        recipeRepository.saveAll(List.of(recipeChiliDTO, recipeSaladDTO));

        given()
                .header(HttpHeaders.CONTENT_TYPE, ContentType.JSON)
                .param("include", "couscous")
                .when()
                .get(API_PATH )
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(1))
                .body("[0].ingredients" , hasItems("100g couscous"));
    }

    @Test
    void filterConditionExcludeIngredients() {
        String[] saladIngredients = { "100g couscous", "2 spring onions", "100ml hot low salt vegetable stock (from a cube is fine)" };
        var recipeSaladDTO = getRecipeEntity("10-minute couscous salad",
                "instructions 10-minute couscous salad", "VEGETARIAN", 2,saladIngredients);

        String[] chiliIngredients = { "100g chorizo", "400g can kidney beans" , "2 spring onions"};
        var recipeChiliDTO = getRecipeEntity("Quick chilli",
                "instructions Quick chilli", "LOW_CARB", 6,chiliIngredients);

        recipeRepository.saveAll(List.of(recipeChiliDTO, recipeSaladDTO));

        given()
                .header(HttpHeaders.CONTENT_TYPE, ContentType.JSON)
                .param("exclude","couscous")
                .when()
                .get(API_PATH )
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(1))
                .body("[0].ingredients" , Matchers.not("100g couscous"));

    }


    @Test
    void filterConditionInvalid() {
        given()
                .header(HttpHeaders.CONTENT_TYPE, ContentType.JSON)
                .param("instructionn", "1")
                .when()
                .get(API_PATH )
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(0));
    }


    @Test
    void filterNoCondition() {
        String[] saladIngredients = { "100g couscous", "2 spring onions", "100ml hot low salt vegetable stock (from a cube is fine)" };
        var recipeSaladDTO = getRecipeEntity("10-minute couscous salad",
                "instructions 10-minute couscous salad", "VEGETARIAN", 2,saladIngredients);

        String[] chiliIngredients = { "100g chorizo", "400g can kidney beans" , "2 spring onions"};
        var recipeChiliDTO = getRecipeEntity("Quick chilli",
                "instructions Quick chilli", "LOW_CARB", 6,chiliIngredients);

        recipeRepository.saveAll(List.of(recipeChiliDTO, recipeSaladDTO));

        given()
                .header(HttpHeaders.CONTENT_TYPE, ContentType.JSON)
                .when()
                .get(API_PATH )
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(2));
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
