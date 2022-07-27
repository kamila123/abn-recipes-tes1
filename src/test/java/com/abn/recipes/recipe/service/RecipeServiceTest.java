package com.abn.recipes.recipe.service;

import com.abn.recipes.entity.Recipe;
import com.abn.recipes.repository.RecipeRepository;
import com.abn.recipes.service.RecipesService;
import com.abn.recipes.service.exception.ResourceAlreadyExistException;
import com.abn.recipes.service.exception.ResourceNotFoundException;
import com.abn.recipes.dto.RecipeDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RecipeServiceTest {

    @InjectMocks
    private RecipesService recipeService;

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @Captor
    private ArgumentCaptor<Recipe> recipeArgumentCaptor;

    @Captor
    private ArgumentCaptor<Query> queryArgumentCaptor;

    private static final String ID = "xpto";

    @Test
    public void whenSaveRecipe_shouldReturnRecipe() {
        Recipe recipe = Recipe.builder().name("Quick chilli").build();
        when(recipeRepository.save(ArgumentMatchers.any(Recipe.class))).thenReturn(recipe);
        Recipe created = recipeRepository.save(recipe);
        assertThat(created.getName()).isSameAs(recipe.getName());
        verify(recipeRepository).save(recipe);
    }

    @Test
    public void whenSaveExistentRecipe_shouldThrow() {
        Recipe existentRecipe = new Recipe();
        existentRecipe.setName("Quick chilli");
        when(recipeRepository.findByName("Quick chilli")).thenReturn(Optional.of(existentRecipe));
        assertThatThrownBy(() -> recipeService.save(RecipeDTO.toDTO(existentRecipe)))
                .isInstanceOf(ResourceAlreadyExistException.class);
    }

    @Test
    public void whenUpdateRecipe_shouldReturnRecipe() {
        String[] chiliIngredients = { "100g chorizo , sliced", "400g can kidney beans" };
        var existentRecipe = getRecipeEntity(                "Quick chilli",
                "instructions Quick chilli", "LOW_CARB", 2,chiliIngredients);

        when(recipeRepository.findById(anyString())).thenReturn(Optional.of(existentRecipe));
        when(recipeRepository.save(any())).thenReturn(existentRecipe);

        var toUpdateRecipe = getRecipeDTO(null,null, "VEGETARIAN", 2,chiliIngredients);

        recipeService.update(ID,toUpdateRecipe);
        verify(recipeRepository, times(1)).save(recipeArgumentCaptor.capture());

        Recipe savedEntity = recipeArgumentCaptor.getValue();
        assertEquals(toUpdateRecipe.getName(), savedEntity.getName());
    }

    @Test
    public void whenUpdateNotFoundRecipe_shouldThrowResourceNotFoundException() {
        String[] chiliIngredients = { "100g chorizo , sliced", "400g can kidney beans" };
        var recipe = getRecipeDTO(                "Quick chilli",
                "instructions Quick chilli", "VEGETARIAN", 2,chiliIngredients);

        assertThatThrownBy(() -> recipeService.update(ID, recipe))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(recipeRepository, times(0)).save(any(Recipe.class));
    }


    @Test
    public void whenDeleteRecipe_shouldFindAndThenDelete() {
        Optional<Recipe> recipe = Optional.of(Recipe.builder().id(ID).build());
        when(recipeRepository.findById(ID)).thenReturn(recipe);
        recipeService.delete(ID);
        verify(recipeRepository, times(1)).deleteById(anyString());
    }

    @Test
    public void whenDeleteRecipeWhereRecipeNotFound_shouldThrowException() {
        assertThatThrownBy(() -> recipeService.delete(ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    public void whenNoFilterConditionsIsPresent_shouldReturnAllRecipes() {
        recipeService.filterCondition(null,null, null, null, null, null);

        verify(recipeRepository, times(1)).findAll(any(Sort.class));
    }

    @Test
    public void whenNameFilterConditionsIsPresent_shouldReturnRecipesFilteredByCategory() {
        recipeService.filterCondition("couscous",null, null, null, null, null);

        verify(mongoTemplate, times(1)).find(queryArgumentCaptor.capture(), eq(Recipe.class));

        Query query = queryArgumentCaptor.getValue();
        String queryString = query.toString();
        assertThat(queryString).contains("\"name\"");
        assertThat(queryString).contains("couscous");
    }

    @Test
    public void whenCategoryFilterConditionsIsPresent_shouldReturnRecipesFilteredByCategory() {
        recipeService.filterCondition(null,"VEGETARIAN", null, null, null, null);

        verify(mongoTemplate, times(1)).find(queryArgumentCaptor.capture(), eq(Recipe.class));

        Query query = queryArgumentCaptor.getValue();
        String queryString = query.toString();
        assertThat(queryString).contains("\"category\"");
        assertThat(queryString).contains("VEGETARIAN");
    }

    @Test
    public void whenServingsFilterConditionsIsPresent_shouldReturnRecipesFilteredByServings() {
        recipeService.filterCondition(null,null, 2, null, null, null);

        verify(mongoTemplate, times(1)).find(queryArgumentCaptor.capture(), eq(Recipe.class));

        Query query = queryArgumentCaptor.getValue();
        String queryString = query.toString();
        assertThat(queryString).contains("\"servings\"");
        assertThat(queryString).contains("2");
    }


    @Test
    public void whenIncludeIngredientsFilterConditionsIsPresent_shouldReturnRecipesFilteredByIngredients() {
        recipeService.filterCondition(null,null, null, "onions", null, null);

        verify(mongoTemplate, times(1)).find(queryArgumentCaptor.capture(), eq(Recipe.class));

        Query query = queryArgumentCaptor.getValue();
        String queryString = query.toString();
        assertThat(queryString).contains("\"ingredients\"");
        assertThat(queryString).contains("onions");
    }

    @Test
    public void whenExcludeIngredientsFilterConditionsIsPresent_shouldReturnRecipesFilteredByExcludedIngredients() {
        recipeService.filterCondition(null,null, null, null, "pepper", null);

        verify(mongoTemplate, times(1)).find(queryArgumentCaptor.capture(), eq(Recipe.class));

        Query query = queryArgumentCaptor.getValue();
        String queryString = query.toString();
        assertThat(queryString).contains("\"ingredients\"");
        assertThat(queryString).contains("pepper");
    }

    @Test
    public void whenInstructionsFilterConditionsIsPresent_shouldReturnRecipesFilteredByInstructions() {
        recipeService.filterCondition(null,null, null, null, null, "bowl");

        verify(mongoTemplate, times(1)).find(queryArgumentCaptor.capture(), eq(Recipe.class));

        Query query = queryArgumentCaptor.getValue();
        String queryString = query.toString();
        assertThat(queryString).contains("\"instructions\"");
        assertThat(queryString).contains("bowl");
    }

    private RecipeDTO getRecipeDTO(String name, String instructions, String category, Integer servings, String[] ingredients) {
        return RecipeDTO.builder()
                .name(name)
                .instructions(instructions)
                .category(category)
                .servings(servings)
                .ingredients(List.of(ingredients))
                .build();
    }

    private Recipe getRecipeEntity(String name, String instructions, String category, Integer servings, String[] ingredients) {
        return Recipe.builder()
                .name(name)
                .instructions(instructions)
                .category(category)
                .servings(servings)
                .ingredients(List.of(ingredients))
                .build();
    }
}

