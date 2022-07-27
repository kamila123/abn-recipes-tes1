package com.abn.recipes.service;

import com.abn.recipes.entity.Recipe;
import com.abn.recipes.repository.RecipeRepository;
import com.abn.recipes.service.exception.ResourceAlreadyExistException;
import com.abn.recipes.service.exception.ResourceNotFoundException;
import com.abn.recipes.dto.RecipeDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Slf4j
@RequiredArgsConstructor
@Service
public class RecipesService {

    private final RecipeRepository recipeRepository;

    private final MongoTemplate mongoTemplate;

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
                .orElseThrow(() -> new ResourceNotFoundException("Recipe with id " + id + " doesn't exist"));

        recipeRepository.deleteById(id);

        log.info("Recipe {} successfully deleted", id);
    }

    public List<Recipe> filterCondition(String name,String category, Integer servings, String include, String exclude, String instructions) {
        final List<Criteria> criteria = new ArrayList<>();
        Sort sortByName = Sort.by(Sort.Direction.ASC, "name");

        if (StringUtils.isNotEmpty(name)) {
            criteria.add(where("name").regex(name));
        }
        if (StringUtils.isNotEmpty(include)) {
            criteria.add(where("ingredients").regex(include));
        }
        if (StringUtils.isNotEmpty(exclude)) {
            criteria.add(where("ingredients").not().regex(exclude));
        }
        if (StringUtils.isNotEmpty(category)) {
            Pattern pattern = Pattern.compile(category, Pattern.CASE_INSENSITIVE);
            criteria.add(where("category").regex(pattern));
        }
        if (Objects.nonNull(servings)) {
            criteria.add(where("servings").is(servings));
        }
        if (StringUtils.isNotEmpty(instructions)) {
            criteria.add(where("instructions").regex(instructions));
        }

        if(!CollectionUtils.isEmpty(criteria)){
            Query query = Query.query(new Criteria().andOperator(criteria.toArray(new Criteria[0]))).with(sortByName);
            return mongoTemplate.find(query, Recipe.class);
        }

        return recipeRepository.findAll(sortByName);
    }

}
