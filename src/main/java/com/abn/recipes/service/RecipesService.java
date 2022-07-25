package com.abn.recipes.service;

import com.abn.recipes.entity.Recipe;
import com.abn.recipes.repository.RecipeRepository;
import com.abn.recipes.service.exception.ResourceAlreadyExistException;
import com.abn.recipes.service.exception.ResourceNotFoundException;
import com.abn.recipes.vo.RecipeDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Slf4j
@RequiredArgsConstructor
@Service
public class RecipesService {

    private final RecipeRepository recipeRepository;

    private final MongoTemplate mongoTemplate;

    private static final ModelMapper modelMapper = new ModelMapper();

    public Recipe save(RecipeDTO recipeVO) {

        recipeRepository.findByName(recipeVO.getName()).ifPresent(r -> {
            throw new ResourceAlreadyExistException("Recipe " + r.getName() + " already exists " );
        });

        var savedRecipe = recipeRepository.save(RecipeDTO.toEntity(recipeVO));

        log.info("Recipe of {} successfully created ", savedRecipe.getName());

        return savedRecipe;
    }


    public Recipe update(String id, RecipeDTO recipeVO) {

        Optional<Recipe> savedRecipe = recipeRepository.findById(id);

        savedRecipe.orElseThrow(() -> new ResourceNotFoundException("Recipe doesn't exist"));

        recipeVO.setId(savedRecipe.get().id);

        var updatedRecipe = recipeRepository.save(RecipeDTO.toEntity(recipeVO));

        log.info("Recipe {} successfully updated", updatedRecipe.getName());

        return updatedRecipe;
    }

    public void delete(String id) {

        recipeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe doesn't exist"));

        recipeRepository.deleteById(id);

        log.info("Recipe {} successfully deleted", id);
    }

    public List<Recipe> filterCondition(String category, Integer servings, String include, String exclude, String instructions) {
        final List<Criteria> criteria = new ArrayList<>();
        if (StringUtils.isNotEmpty(include)) {
            criteria.add(where("ingredients").in(include));
        }
        if (StringUtils.isNotEmpty(exclude)) {
            criteria.add(where("ingredients").not().in(exclude));
        }
        if (StringUtils.isNotEmpty(category)) {
            criteria.add(where("category").is(category));
        }
        if (Objects.nonNull(servings)) {
            criteria.add(where("servings").is(servings));
        }
        if (StringUtils.isNotEmpty(instructions)) {
            criteria.add(where("instructions").regex(instructions));
        }

        if(!CollectionUtils.isEmpty(criteria)){
            return mongoTemplate.find(Query.query(new Criteria().andOperator(criteria.toArray(new Criteria[0]))), Recipe.class);
        }

        return recipeRepository.findAll();
    }

}
