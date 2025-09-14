package com.knusrae.cook.api.web;

import com.knusrae.cook.api.dto.RecipeDto;
import com.knusrae.cook.api.service.RecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recipe")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService recipeService;

    @GetMapping("/recipe-list")
    public ResponseEntity<List<RecipeDto>> getRecipeList() {
        List<RecipeDto> recipeList = recipeService.getRecipeList();
        return ResponseEntity.ok(recipeList);
    }
}
