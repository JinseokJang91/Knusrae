import type { Recipe } from './recipe';

export interface RecipeBook {
    id: number;
    memberId: number;
    name: string;
    color: string;
    sortOrder: number;
    bookmarkCount: number;
    createdAt: string;
    updatedAt: string;
}

export interface RecipeBookmark {
    id: number;
    recipeBookId: number;
    recipeId: number;
    memberId: number;
    /** 사용자가 해당 북마크(레시피)에 남긴 메모 */
    memo?: string | null;
    recipe: Recipe;
    createdAt: string;
}

export interface RecipeBookColor {
    name: string;
    label: string;
    hex: string;
}

// 레시피북 색상 옵션
export const RECIPE_BOOK_COLORS: RecipeBookColor[] = [
    { name: 'blue', label: '파란색', hex: '#3b82f6' },
    { name: 'red', label: '빨간색', hex: '#ef4444' },
    { name: 'green', label: '초록색', hex: '#10b981' },
    { name: 'yellow', label: '노란색', hex: '#f59e0b' },
    { name: 'purple', label: '보라색', hex: '#8b5cf6' },
    { name: 'pink', label: '분홍색', hex: '#ec4899' },
    { name: 'gray', label: '회색', hex: '#6b7280' },
    { name: 'orange', label: '주황색', hex: '#f97316' }
];

export function getRecipeBookColorHex(colorName: string): string {
    const color = RECIPE_BOOK_COLORS.find(c => c.name === colorName);
    return color?.hex || '#3b82f6';
}

export function getRecipeBookColorLabel(colorName: string): string {
    const color = RECIPE_BOOK_COLORS.find(c => c.name === colorName);
    return color?.label || '파란색';
}
