package com.knusrae.cook.api.web;

import com.knusrae.cook.api.dto.RecipeDto;
import com.knusrae.cook.api.service.RecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recipe")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService recipeService;

    @GetMapping("/list")
    public ResponseEntity<List<RecipeDto>> getRecipeList() {
        List<RecipeDto> recipeList = recipeService.getRecipeList();
        return ResponseEntity.ok(recipeList);
    }

    @PostMapping("/save")
    public ResponseEntity<Void> saveRecipe(@RequestBody RecipeDto recipeDto) {
        recipeService.saveRecipe(recipeDto);
        return ResponseEntity.ok().build();
    }
}
