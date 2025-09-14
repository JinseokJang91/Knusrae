package com.knusrae.cook.api.service;

import com.knusrae.cook.api.domain.Recipe;
import com.knusrae.cook.api.dto.RecipeDto;
import com.knusrae.cook.api.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecipeService {

    private final RecipeRepository recipeRepository;

    public List<RecipeDto> getRecipeList() {
        List<Recipe> recipeList = recipeRepository.findAllByOrderByCreatedAtDesc();

        return recipeList.stream()
                .map(RecipeDto::new)
                .toList();
    }
}
