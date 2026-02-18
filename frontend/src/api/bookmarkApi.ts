import { httpJson } from '@/utils/http';
import { getApiBaseUrl } from '@/utils/constants';
import type { RecipeBook, RecipeBookmark } from '@/types/bookmark';

const BASE_URL = getApiBaseUrl('cook');

// ============================================
// 레시피북 관리 API
// ============================================

export async function getRecipeBooks(): Promise<RecipeBook[]> {
    const response = await httpJson<RecipeBook[] | { data?: RecipeBook[] }>(
        BASE_URL,
        '/api/recipe/bookmarks/recipe-books',
        { method: 'GET' }
    );
    return Array.isArray(response) ? response : response.data ?? [];
}

export async function createRecipeBook(name: string, color: string): Promise<RecipeBook> {
    return await httpJson<RecipeBook>(
        BASE_URL,
        '/api/recipe/bookmarks/recipe-books',
        {
            method: 'POST',
            body: JSON.stringify({ name, color })
        }
    );
}

export async function updateRecipeBook(
    recipeBookId: number,
    name: string,
    color: string
): Promise<RecipeBook> {
    return await httpJson<RecipeBook>(
        BASE_URL,
        `/api/recipe/bookmarks/recipe-books/${recipeBookId}`,
        {
            method: 'PUT',
            body: JSON.stringify({ name, color })
        }
    );
}

export async function deleteRecipeBook(recipeBookId: number): Promise<void> {
    await httpJson(BASE_URL, `/api/recipe/bookmarks/recipe-books/${recipeBookId}`, {
        method: 'DELETE'
    });
}

export async function reorderRecipeBooks(recipeBookIdOrder: number[]): Promise<RecipeBook[]> {
    return await httpJson<RecipeBook[]>(
        BASE_URL,
        '/api/recipe/bookmarks/recipe-books/reorder',
        {
            method: 'PUT',
            body: JSON.stringify({ recipeBookIdOrder })
        }
    );
}

export async function createDefaultRecipeBook(): Promise<RecipeBook> {
    return await httpJson<RecipeBook>(
        BASE_URL,
        '/api/recipe/bookmarks/recipe-books/default',
        { method: 'POST' }
    );
}

// ============================================
// 북마크 관리 API
// ============================================

export async function getBookmarksByRecipeBook(recipeBookId: number): Promise<RecipeBookmark[]> {
    const response = await httpJson<RecipeBookmark[] | { data?: RecipeBookmark[] }>(
        BASE_URL,
        `/api/recipe/bookmarks/recipe-books/${recipeBookId}/bookmarks`,
        { method: 'GET' }
    );
    return Array.isArray(response) ? response : response.data ?? [];
}

export async function addBookmark(
    recipeBookId: number,
    recipeId: number,
    memo?: string | null
): Promise<RecipeBookmark> {
    return await httpJson<RecipeBookmark>(BASE_URL, '/api/recipe/bookmarks', {
        method: 'POST',
        body: JSON.stringify({ recipeBookId, recipeId, memo: memo ?? undefined })
    });
}

export async function updateBookmarkMemo(bookmarkId: number, memo: string | null): Promise<RecipeBookmark> {
    return await httpJson<RecipeBookmark>(BASE_URL, `/api/recipe/bookmarks/${bookmarkId}/memo`, {
        method: 'PUT',
        body: JSON.stringify({ memo: memo ?? '' })
    });
}

export async function removeBookmark(recipeBookId: number, recipeId: number): Promise<void> {
    await httpJson(
        BASE_URL,
        `/api/recipe/bookmarks?recipeBookId=${recipeBookId}&recipeId=${recipeId}`,
        { method: 'DELETE' }
    );
}

export async function checkBookmark(
    recipeId: number
): Promise<{ recipeBooks: number[]; isBookmarked: boolean }> {
    return await httpJson<{ recipeBooks: number[]; isBookmarked: boolean }>(
        BASE_URL,
        `/api/recipe/bookmarks/check/${recipeId}`,
        { method: 'GET' }
    );
}

export async function moveBookmark(
    bookmarkId: number,
    targetRecipeBookId: number
): Promise<RecipeBookmark> {
    return await httpJson<RecipeBookmark>(
        BASE_URL,
        `/api/recipe/bookmarks/${bookmarkId}/move`,
        {
            method: 'PUT',
            body: JSON.stringify({ targetRecipeBookId })
        }
    );
}
