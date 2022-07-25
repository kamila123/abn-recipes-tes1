package com.abn.recipes.recipe.controller;

import com.abn.recipes.entity.Recipe;
import com.abn.recipes.recipe.controller.config.BaseTest;
import com.abn.recipes.repository.RecipeRepository;
import com.abn.recipes.vo.ErrorResponse;
import com.abn.recipes.vo.RecipeDTO;
import io.restassured.http.ContentType;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class RecipeControllerTest extends BaseTest {

    @Autowired
    private RecipeRepository recipeRepository;

    @BeforeEach
    public void clean() {
        recipeRepository.deleteAll();
    }

    @Test
    void create() {
        String[] chiliIngredients = { "100g chorizo , sliced", "400g can kidney beans" };
        var recipeDTO = getRecipeEntity(                "Quick chilli",
                "instructions Quick chilli", "LOW_CARB", 2,chiliIngredients);

        given()
                .body(recipeDTO)
                .header(HttpHeaders.CONTENT_TYPE, ContentType.JSON)
                .when()
                .post("/v1/recipe")
                .then()
                .statusCode(HttpStatus.OK.value());

        var savedRecipe = recipeRepository.findByName(recipeDTO.getName());
        assertThat(savedRecipe.isPresent()).isTrue();

        Recipe recipe = savedRecipe.get();
        assertThat(recipe.getName()).isEqualTo(recipeDTO.getName());
        assertThat(recipe.getInstructions()).isEqualTo(recipeDTO.getInstructions());
        assertThat(recipe.getCategory()).isEqualTo(recipeDTO.getCategory());
        assertThat(recipe.getServings()).isEqualTo(recipeDTO.getServings());
        assertThat(recipe.getIngredients()).isEqualTo(recipeDTO.getIngredients());
    }

    @Test
    void createDuplicatedRecipe() {
        String[] chiliIngredients = { "100g chorizo , sliced", "400g can kidney beans" };
        var recipeDTO = getRecipeEntity(                "Quick chilli",
                "instructions Quick chilli", "LOW_CARB", 2,chiliIngredients);

        given()
                .body(recipeDTO)
                .header(HttpHeaders.CONTENT_TYPE, ContentType.JSON)
                .when()
                .post("/v1/recipe")
                .then()
                .statusCode(HttpStatus.OK.value());

        var savedRecipe = recipeRepository.findByName(recipeDTO.getName());
        assertThat(savedRecipe.isPresent()).isTrue();

        Recipe recipe = savedRecipe.get();
        assertThat(recipe.getName()).isEqualTo(recipeDTO.getName());
        assertThat(recipe.getInstructions()).isEqualTo(recipeDTO.getInstructions());
        assertThat(recipe.getCategory()).isEqualTo(recipeDTO.getCategory());
        assertThat(recipe.getServings()).isEqualTo(recipeDTO.getServings());
        assertThat(recipe.getIngredients()).isEqualTo(recipeDTO.getIngredients());

        given()
                .body(recipeDTO)
                .header(HttpHeaders.CONTENT_TYPE, ContentType.JSON)
                .when()
                .post("/v1/recipe")
                .then()
                .statusCode(HttpStatus.CONFLICT.value());
    }

    @Test
    void update() {
        String[] chiliIngredients = { "100g chorizo , sliced", "400g can kidney beans" };
        var recipeEntity = getRecipeEntity(                "Quick chilli",
                "instructions Quick chilli", "LOW_CARB", 2,chiliIngredients);

        var recipeSaved = recipeRepository.save(recipeEntity);

        var recipeDTO = getRecipeEntity(                "Quick chilli",
                "instructions Quick chilli", "LOW_CARB", 2,chiliIngredients);

        given()
                .body(recipeDTO)
                .header(HttpHeaders.CONTENT_TYPE, ContentType.JSON)
                .when()
                .put("/v1/recipe/" + recipeSaved.getId())
                .then()
                .statusCode(HttpStatus.OK.value());

        var maybeRecipe = recipeRepository.findByName(recipeDTO.getName());
        assertThat(maybeRecipe.isPresent()).isTrue();

        Recipe recipeUpdated = maybeRecipe.get();
        assertThat(recipeUpdated.getName()).isEqualTo(recipeDTO.getName());
        assertThat(recipeUpdated.getInstructions()).isEqualTo(recipeDTO.getInstructions());
        assertThat(recipeUpdated.getCategory()).isEqualTo(recipeDTO.getCategory());
        assertThat(recipeUpdated.getServings()).isEqualTo(recipeDTO.getServings());
        assertThat(recipeUpdated.getIngredients()).isEqualTo(recipeDTO.getIngredients());
    }

    @Test
    void updateNonExistingId() {
        String[] chiliIngredients = { "100g chorizo , sliced", "400g can kidney beans" };

        var recipeDTO = getRecipeEntity(                "Quick chilli",
                "instructions Quick chilli", "LOW_CARB", 2,chiliIngredients);

        ErrorResponse errorResponse = given()
                .body(recipeDTO)
                .header(HttpHeaders.CONTENT_TYPE, ContentType.JSON)
                .when()
                .put("/v1/recipe/xpto")
                .then()
                .statusCode(404)
                .extract()
                .as(ErrorResponse.class);

        assertThat(errorResponse.message()).isEqualTo("Recipe doesn't exist");
    }

    @Test
    void filterConditions() {
        String[] saladIngredients = { "100g couscous", "2 spring onions", "100ml hot low salt vegetable stock (from a cube is fine)" };
        var recipeSaladDTO = getRecipeEntity("10-minute couscous salad",
                "instructions 10-minute couscous salad", "LOW_CARB", 2,saladIngredients);

        String[] chiliIngredients = { "100g chorizo , sliced", "400g can kidney beans" };
        var recipeChiliDTO = getRecipeEntity("Quick chilli",
                "instructions Quick chilli", "LOW_CARB", 2,chiliIngredients);

        recipeRepository.saveAll(List.of(recipeSaladDTO, recipeChiliDTO));

        var recipesArray = given()
                .header(HttpHeaders.CONTENT_TYPE, ContentType.JSON)
                .when()
                .get("/v1/recipe")
                .then()
                .statusCode(200)
                .extract()
                .as(RecipeDTO[].class);

        var recipes = Arrays.asList(recipesArray);
        assertThat(recipes.size()).isEqualTo(2);
    }

    @Test
    void delete() {
        String[] saladIngredients = { "100g couscous", "2 spring onions", "100ml hot low salt vegetable stock (from a cube is fine)" };
        var recipeSaladDTO = getRecipeEntity("10-minute couscous salad",
                "instructions 10-minute couscous salad", "LOW_CARB", 2,saladIngredients);

        var recipeSaved = recipeRepository.save(recipeSaladDTO);

        given()
                .header(HttpHeaders.CONTENT_TYPE, ContentType.JSON)
                .when()
                .delete("/v1/recipe/" + recipeSaved.getId())
                .then()
                .statusCode(200);

        var allRecipes = recipeRepository.findAll();
        assertThat(allRecipes.isEmpty()).isTrue();
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

        var recipesArray = given()
                .header(HttpHeaders.CONTENT_TYPE, ContentType.JSON)
                .when()
                .get("/v1/recipe?category=VEGETARIAN")
                .then()
                .statusCode(200)
                .extract()
                .as(RecipeDTO[].class);

        var recipes = Arrays.asList(recipesArray);
        assertThat(recipes.size()).isEqualTo(1);

        RecipeDTO recipeFound = recipes.get(0);
        assertThat(recipeFound.getCategory()).isEqualTo(recipeSaladDTO.getCategory());
        assertThat(recipeFound.getName()).isEqualTo(recipeSaladDTO.getName());
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

        var recipesArray = given()
                .header(HttpHeaders.CONTENT_TYPE, ContentType.JSON)
                .when()
                .get("/v1/recipe?category=vegetarian")
                .then()
                .statusCode(200)
                .extract()
                .as(RecipeDTO[].class);

        var recipes = Arrays.asList(recipesArray);
        assertThat(recipes.size()).isEqualTo(1);

        RecipeDTO recipeFound = recipes.get(0);
        assertThat(recipeFound.getCategory()).isEqualTo(recipeSaladDTO.getCategory());
        assertThat(recipeFound.getName()).isEqualTo(recipeSaladDTO.getName());
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

        var recipesArray = given()
                .header(HttpHeaders.CONTENT_TYPE, ContentType.JSON)
                .when()
                .get("/v1/recipe?instructions=couscous")
                .then()
                .statusCode(200)
                .extract()
                .as(RecipeDTO[].class);

        var recipes = Arrays.asList(recipesArray);
        assertThat(recipes.size()).isEqualTo(1);

        RecipeDTO recipeFound = recipes.get(0);
        assertThat(recipeFound.getInstructions()).contains("couscous");
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

        var recipesArray = given()
                .header(HttpHeaders.CONTENT_TYPE, ContentType.JSON)
                .when()
                .get("/v1/recipe?servings=2")
                .then()
                .statusCode(200)
                .extract()
                .as(RecipeDTO[].class);

        var recipes = Arrays.asList(recipesArray);
        assertThat(recipes.size()).isEqualTo(1);

        RecipeDTO recipeFound = recipes.get(0);
        assertThat(recipeFound.getServings()).isEqualTo(2);
        assertThat(recipeFound.getName()).isEqualTo(recipeSaladDTO.getName());
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

        var recipesArray = given()
                .header(HttpHeaders.CONTENT_TYPE, ContentType.JSON)
                .when()
                .get("/v1/recipe?include=100g couscous")
                .then()
                .statusCode(200)
                .extract()
                .as(RecipeDTO[].class);

        var recipes = Arrays.asList(recipesArray);
        assertThat(recipes.size()).isEqualTo(1);

        RecipeDTO recipeFound = recipes.get(0);
        assertThat(recipeFound.getIngredients()).contains("100g couscous");
        assertThat(recipeFound.getName()).isEqualTo(recipeSaladDTO.getName());
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

        var recipesArray = given()
                .header(HttpHeaders.CONTENT_TYPE, ContentType.JSON)
                .when()
                .get("/v1/recipe?exclude=100g chorizo")
                .then()
                .statusCode(200)
                .extract()
                .as(RecipeDTO[].class);

        var recipes = Arrays.asList(recipesArray);
        assertThat(recipes.size()).isEqualTo(1);

        RecipeDTO recipeFound = recipes.get(0);
        assertThat(recipeFound.getIngredients()).doesNotContain("100g chorizo");
        assertThat(recipeFound.getName()).isEqualTo(recipeSaladDTO.getName());
    }


    @Test
    void filterConditionInvalid() {
        var recipesArray = given()
                .header(HttpHeaders.CONTENT_TYPE, ContentType.JSON)
                .when()
                .get("/v1/recipe?instructionn=1")
                .then()
                .statusCode(200)
                .extract()
                .as(RecipeDTO[].class);

        var recipes = Arrays.asList(recipesArray);
        assertThat(recipes.size()).isEqualTo(0);
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
        var recipesArray = given()
                .header(HttpHeaders.CONTENT_TYPE, ContentType.JSON)
                .when()
                .get("/v1/recipe")
                .then()
                .statusCode(200)
                .extract()
                .as(RecipeDTO[].class);

        var recipes = Arrays.asList(recipesArray);
        assertThat(recipes.size()).isEqualTo(2);
    }
}
