// Recipe 관련 타입 정의

export interface RecipeCategory {
    codeId: string;
    detailCodeId: string;
    codeName?: string;
    detailName?: string;
}

export interface RecipeCookingTip {
    codeId: string;
    detailCodeId: string;
    detailName?: string;
}

export interface RecipeIngredientItem {
    name: string;
    quantity: string | null; // 수량 (분수 입력 지원: "1/2", "3/4", "1.5" 등)
    codeId?: string;
    detailCodeId?: string;
    codeName?: string;
    detailName?: string;
    customUnitName?: string; // 직접 입력한 단위 이름
}

export interface RecipeIngredientGroup {
    order: number;
    codeId?: string;
    detailCodeId?: string;
    codeName?: string;
    detailName?: string;
    customTypeName?: string; // 직접 입력한 그룹 타입 이름
    items: RecipeIngredientItem[];
}

export interface RecipeStep {
    order: number;
    text: string;
    imageUrl?: string;
}

export interface RecipeImage {
    id?: number;
    url: string;
    storageKey?: string;
    fileName?: string;
    contentType?: string;
    size?: number;
    sortOrder?: number;
    isMainImage: boolean;
}

export interface RecipeStats {
    totalComments: number;
    totalReviews: number;
    averageRating: number;
    totalLikes: number;
    isLiked: boolean;
    favoriteCount?: number;
}

export interface RecipeDetail {
    id: number;
    title: string;
    introduction: string;
    description?: string;
    memberId: number;
    memberName: string;
    memberNickname?: string;
    memberProfileImage?: string;
    categories: RecipeCategory[];
    cookingTips: RecipeCookingTip[];
    ingredientGroups: RecipeIngredientGroup[];
    steps: RecipeStep[];
    images: RecipeImage[];
    stats: RecipeStats;
    status: 'DRAFT' | 'PUBLISHED';
    visibility: 'PUBLIC' | 'PRIVATE';
    thumbnail?: string;
    hits?: number;
    createdAt?: string;
    updatedAt?: string;
}

export interface RecipeComment {
    id: number;
    recipeId: number;
    memberId: number;
    memberName: string;
    memberNickname?: string;
    memberProfileImage?: string;
    content: string;
    imageUrl?: string;
    parentId?: number;
    parentMemberId?: number;
    parentMemberNickname?: string;
    children?: RecipeComment[];
    createdAt: string;
    updatedAt?: string;
}

export interface Recipe {
    id: number;
    title: string;
    status: 'DRAFT' | 'PUBLISHED';
    visibility: 'PUBLIC' | 'PRIVATE';
    thumbnail?: string;
    introduction?: string;
    description?: string;
    categories?: RecipeCategory[];
    hits?: number;
    createdAt?: string;
    updatedAt?: string;
    memberId: number;
    memberName?: string;
    memberNickname?: string;
    memberProfileImage?: string;
    commentCount?: number;
    favoriteCount?: number;
    // 검색 결과에서 사용되는 추가 속성
    isFavorite?: boolean;
    category?: string;
    cookingTime?: string;
    servings?: string;
}

/** 찜 목록 API 응답 항목 */
export interface FavoriteItem {
    id: number;
    recipeId: number;
    recipe: Recipe & { cookingTips?: RecipeCookingTip[] };
    createdAt: string;
}

// 인기도 통계
export interface PopularityStats {
    popularityScore: number;
    hits24h: number;
    hits7d: number;
    favoriteCount: number;
    commentCount: number;
    favoriteIncrease24h?: number;
}

// 트렌드 상태
export type TrendStatus = 'UP' | 'DOWN' | 'NEW' | 'SAME';

// 인기 레시피 항목
export interface PopularRecipeItem {
    rank: number;
    previousRank?: number;
    trendStatus: TrendStatus;
    recipe: Recipe;
    popularityStats: PopularityStats;
    calculatedAt?: string;
}

// 인기 레시피 API 응답
export interface PopularRecipesResponse {
    recipes: PopularRecipeItem[];
    period: string;
    calculatedAt?: string;
    totalCount?: number;
}

// 레시피 조회 기록
export interface RecipeView {
    id: number;
    memberId: number;
    recipeId: number;
    viewedAt: string;
    recipe?: RecipeSimple;
}

// 간단한 레시피 정보 (조회 기록용)
export interface RecipeSimple {
    id: number;
    title: string;
    thumbnail?: string;
    memberNickname?: string;
    hits?: number;
    favoriteCount?: number;
    commentCount?: number;
}

// 최근 본 레시피 API 응답
export interface RecentViewsResponse {
    views: RecipeView[];
    totalCount: number;
}

// 조회 기록 생성 API 응답
export interface CreateViewResponse {
    view: RecipeView;
    isNew: boolean;
}

// 추천 레시피
export interface RecommendedRecipe {
    id: number;
    title: string;
    description?: string;
    thumbnail?: string;
    memberId: number;
    memberNickname?: string;
    memberProfileImage?: string;
    hits?: number;
    categories?: RecipeCategory[];
    commentCount?: number;
    favoriteCount?: number;
    createdAt?: string;
    recommendReason?: string; // 추천 이유
}

// 오늘의 레시피 추천 API 응답
export interface TodayRecommendationsResponse {
    recipes: RecommendedRecipe[];
    recommendationType: 'PERSONALIZED' | 'GENERAL';
    refreshable: boolean;
}

