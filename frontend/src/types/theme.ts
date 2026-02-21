/**
 * 테마 컬렉션 타입
 */
export interface ThemeCollection {
    id: number;
    name: string;
    description?: string;
    thumbnailImage?: string;
    startDate?: string;
    endDate?: string;
    status: string;
    sortOrder: number;
    recipeCount: number;
    createdAt: string;
}

/**
 * 테마 내 레시피 아이템 타입
 */
export interface ThemeRecipeItem {
    recipeId: number;
    title: string;
    thumbnail?: string;
    memberId: number;
    memberNickname?: string;
    hits: number;
    commentCount: number;
    favoriteCount: number;
    sortOrder: number;
}

/**
 * 테마 상세 응답 타입 (테마 정보 + 레시피 목록)
 */
export interface ThemeCollectionDetail {
    theme: ThemeCollection;
    recipes: ThemeRecipeItem[];
    totalCount: number;
}
