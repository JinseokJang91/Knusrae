// Category 관련 타입 정의

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
    recipes: any[]; // Recipe 타입을 사용
    totalCount: number;
}
