export interface IngredientGroup {
    id: number;
    name: string;
    imageUrl?: string;
    sortOrder: number;
}

export interface Ingredient {
    id: number;
    name: string;
    group: IngredientGroup;
    imageUrl?: string;
    sortOrder: number;
}

export interface IngredientStorage {
    id: number;
    ingredient: Ingredient;
    content: string;
    summary: string;
    createdAt: string;
    updatedAt: string;
}

export interface IngredientPreparation {
    id: number;
    ingredient: Ingredient;
    content: string;
    summary: string;
    createdAt: string;
    updatedAt: string;
}

export interface IngredientRequest {
    ingredientName: string;
    requestType: 'STORAGE' | 'PREPARATION';
    message?: string;
}

export interface IngredientRequestResponse {
    id: number;
    ingredientName: string;
    requestType: string;
    message?: string;
    memberId?: number;
    status: string;
    createdAt: string;
    updatedAt: string;
}

export type IngredientType = 'storage' | 'preparation';
