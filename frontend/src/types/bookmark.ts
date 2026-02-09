import type { Recipe } from './recipe';

export interface BookmarkFolder {
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
    folderId: number;
    recipeId: number;
    memberId: number;
    recipe: Recipe;
    createdAt: string;
}

export interface FolderColor {
    name: string;
    label: string;
    hex: string;
}

// 폴더 색상 옵션
export const FOLDER_COLORS: FolderColor[] = [
    { name: 'blue', label: '파란색', hex: '#3b82f6' },
    { name: 'red', label: '빨간색', hex: '#ef4444' },
    { name: 'green', label: '초록색', hex: '#10b981' },
    { name: 'yellow', label: '노란색', hex: '#f59e0b' },
    { name: 'purple', label: '보라색', hex: '#8b5cf6' },
    { name: 'pink', label: '분홍색', hex: '#ec4899' },
    { name: 'gray', label: '회색', hex: '#6b7280' },
    { name: 'orange', label: '주황색', hex: '#f97316' }
];

// 색상 이름으로 hex 코드 가져오기
export function getFolderColorHex(colorName: string): string {
    const color = FOLDER_COLORS.find(c => c.name === colorName);
    return color?.hex || '#3b82f6'; // 기본값: blue
}

// 색상 이름으로 라벨 가져오기
export function getFolderColorLabel(colorName: string): string {
    const color = FOLDER_COLORS.find(c => c.name === colorName);
    return color?.label || '파란색';
}
