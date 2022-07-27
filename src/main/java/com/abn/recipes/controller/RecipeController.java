package com.abn.recipes.controller;

import com.abn.recipes.entity.Recipe;
import com.abn.recipes.service.RecipesService;
import com.abn.recipes.dto.RecipeDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Tag(name = "Recipes", description = "Recipes management endpoint api")
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/recipe")
public class RecipeController {

    private final RecipesService recipeService;

    @PostMapping
    @Operation(method = "Add a new recipe", summary = "Add recipe")
    public ResponseEntity<RecipeDTO> add(@RequestBody RecipeDTO recipeDTO){
        var r = RecipeDTO.toDTO(recipeService.save(recipeDTO));
        return ResponseEntity.ok(r);
    }

    @Operation(method = "Update a recipe", summary = "Update recipe")
    @PutMapping("/{id}")
    public ResponseEntity<Recipe> updateRecipe(@PathVariable String id, @RequestBody @Valid RecipeDTO recipeVO) {
        var r = recipeService.update(id, recipeVO);
        return ResponseEntity.ok(r);
    }

    @Operation(method = "Delete a recipe", summary = "Delete a recipe")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> removeRecipe(@PathVariable String id) {
        recipeService.delete(id);
        return ResponseEntity.ok("successfully deleted " + id);
    }

    @Operation(method = "Find recipes by filters", summary = "Find recipes")
    @GetMapping
    public List<RecipeDTO> findRecipes(@RequestParam(required = false) String name,
                                       @RequestParam(required = false) Integer servings,
                                       @RequestParam(required = false) String exclude,
                                       @RequestParam(required = false) String include,
                                       @RequestParam(required = false) String category,
                                       @RequestParam(required = false) String instructions) {
        return recipeService.filterCondition(name, category, servings, include, exclude, instructions)
                .stream()
                .map(RecipeDTO::toDTO)
                .toList();
    }
}
