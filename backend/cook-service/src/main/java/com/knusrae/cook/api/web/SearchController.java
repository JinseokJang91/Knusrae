package com.knusrae.cook.api.web;

import com.knusrae.cook.api.dto.RecipeDto;
import com.knusrae.cook.api.domain.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 검색 기능을 제공하는 컨트롤러
 * 메인 화면, 카테고리, 자유 게시판 등 여러 곳에서 사용할 수 있는 공통 검색 모듈
 */
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Slf4j
public class SearchController {
    private final SearchService searchService;

    /**
     * 레시피 제목으로 검색
     * 
     * @param keyword 검색어
     * @return 제목에 검색어가 포함된 공개 레시피 목록
     */
    @GetMapping("/recipes")
    public ResponseEntity<List<RecipeDto>> searchRecipes(@RequestParam(required = false) String keyword) {
        log.debug("Searching recipes with keyword: {}", keyword);
        
        List<RecipeDto> recipeList = searchService.searchRecipesByTitle(keyword);
        log.info("Found {} recipes matching keyword: {}", recipeList.size(), keyword);
        
        return ResponseEntity.ok(recipeList);
    }
}

