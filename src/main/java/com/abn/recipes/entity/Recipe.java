package com.abn.recipes.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document("recipes")
@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class Recipe {
    @Id
    public String id;
    public String name;
    public String instructions;
    public String category;
    public Integer servings;
    public List<String> ingredients;
}
