package com.abn.recipes.dto;

import com.abn.recipes.entity.Recipe;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
public class RecipeDTO {
    public String id ;
    @NotBlank
    public String name ;
    @NotBlank
    public String instructions;
    @NotNull
    public String category;
    @NotNull
    public Integer servings;
    @NotEmpty
    public List<String> ingredients;

    public static Recipe toEntity(RecipeDTO dto){
        return  Recipe.builder()
                .id(dto.getId())
                .name(dto.getName())
                .instructions(dto.getInstructions())
                .category(dto.getCategory())
                .servings(dto.getServings())
                .ingredients(dto.getIngredients())
                .build();
    }

    public static RecipeDTO toDTO(Recipe recipes){
        return  RecipeDTO.builder()
                .id(recipes.getId())
                .name(recipes.getName())
                .instructions(recipes.getInstructions())
                .category(recipes.getCategory())
                .servings(recipes.getServings())
                .ingredients(recipes.getIngredients())
                .build();
    }
}
