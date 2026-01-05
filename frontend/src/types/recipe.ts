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
}

