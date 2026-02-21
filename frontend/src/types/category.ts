// Category 관련 타입 정의
import type { Recipe, RecipeCookingTip, RecipeCategory } from './recipe';

export interface CategoryInfo {
    codeId: string;
    detailCodeId: string;
    codeName?: string;
    detailName: string;
    recipeCount?: number;
    totalHits?: number;
    reason?: string; // TRENDING | PERSONALIZED | SEASONAL
}

export interface TrendingCategoriesResponse {
    categories: CategoryInfo[];
    period: string;
}

export interface CategoryRecipesResponse {
    category: CategoryInfo;
    recipes: Recipe[];
    totalCount: number;
}

/** 공통코드 상세(서브 카테고리) — loadCategories 등에서 사용 */
export interface CategoryDetail {
    detailCodeId: string;
    codeName: string;
}

/** 공통코드 메인 카테고리 (codeName + details) */
export interface MainCategory {
    codeId: string;
    codeName: string;
    details: CategoryDetail[];
}

/** 카테고리 페이지 UI용 카테고리 (탭/칩 표시) */
export interface Category {
    value: string;
    name: string;
    description: string;
    icon: string;
    color: string;
    recipeCount: number;
    mainCategoryId?: string;
}

/** 카테고리 페이지 목록용 레시피 (API Recipe + UI 필드) */
export type CategoryRecipeItem = Omit<Recipe, 'category' | 'cookingTime' | 'servings' | 'commentCount'> & {
    category: string | null;
    categoryIds: string[];
    categoryKeys: string[];
    primaryCategoryName: string | null;
    cookingTime: string | null;
    servings: string | null;
    isFavorite: boolean;
    commentCount: number;
};

/** 카테고리 검색(자동완성) 항목 */
export interface CategorySearchItem {
    label: string;
    mainCodeId: string;
    detailCodeId: string | null;
}

/** 정렬 옵션 (라벨/값) */
export interface SortOption {
    label: string;
    value: string;
}

// recipe.ts 타입 재export (Category.vue에서 사용)
export type { RecipeCategory, RecipeCookingTip };
