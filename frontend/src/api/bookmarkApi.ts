import { httpJson } from '@/utils/http';
import { getApiBaseUrl } from '@/utils/constants';
import type { BookmarkFolder, RecipeBookmark } from '@/types/bookmark';

const BASE_URL = getApiBaseUrl('cook');

// ============================================
// 폴더 관리 API
// ============================================

/**
 * 회원의 폴더 목록 조회
 */
export async function getFolders(): Promise<BookmarkFolder[]> {
    const response = await httpJson<BookmarkFolder[] | { data?: BookmarkFolder[] }>(
        BASE_URL,
        '/api/recipe/bookmarks/folders',
        { method: 'GET' }
    );
    return Array.isArray(response) ? response : response.data ?? [];
}

/**
 * 폴더 생성
 */
export async function createFolder(name: string, color: string): Promise<BookmarkFolder> {
    return await httpJson<BookmarkFolder>(
        BASE_URL,
        '/api/recipe/bookmarks/folders',
        {
            method: 'POST',
            body: JSON.stringify({ name, color })
        }
    );
}

/**
 * 폴더 수정
 */
export async function updateFolder(
    folderId: number,
    name: string,
    color: string
): Promise<BookmarkFolder> {
    return await httpJson<BookmarkFolder>(
        BASE_URL,
        `/api/recipe/bookmarks/folders/${folderId}`,
        {
            method: 'PUT',
            body: JSON.stringify({ name, color })
        }
    );
}

/**
 * 폴더 삭제
 */
export async function deleteFolder(folderId: number): Promise<void> {
    await httpJson(BASE_URL, `/api/recipe/bookmarks/folders/${folderId}`, {
        method: 'DELETE'
    });
}

/**
 * 폴더 순서 변경
 */
export async function reorderFolders(folderIdOrder: number[]): Promise<BookmarkFolder[]> {
    return await httpJson<BookmarkFolder[]>(
        BASE_URL,
        '/api/recipe/bookmarks/folders/reorder',
        {
            method: 'PUT',
            body: JSON.stringify({ folderIdOrder })
        }
    );
}

/**
 * 기본 폴더 자동 생성
 */
export async function createDefaultFolder(): Promise<BookmarkFolder> {
    return await httpJson<BookmarkFolder>(
        BASE_URL,
        '/api/recipe/bookmarks/folders/default',
        { method: 'POST' }
    );
}

// ============================================
// 북마크 관리 API
// ============================================

/**
 * 폴더별 북마크 조회
 */
export async function getBookmarksByFolder(folderId: number): Promise<RecipeBookmark[]> {
    const response = await httpJson<RecipeBookmark[] | { data?: RecipeBookmark[] }>(
        BASE_URL,
        `/api/recipe/bookmarks/folder/${folderId}`,
        { method: 'GET' }
    );
    return Array.isArray(response) ? response : response.data ?? [];
}

/**
 * 북마크 추가
 */
export async function addBookmark(folderId: number, recipeId: number): Promise<RecipeBookmark> {
    return await httpJson<RecipeBookmark>(BASE_URL, '/api/recipe/bookmarks', {
        method: 'POST',
        body: JSON.stringify({ folderId, recipeId })
    });
}

/**
 * 북마크 제거
 */
export async function removeBookmark(folderId: number, recipeId: number): Promise<void> {
    await httpJson(
        BASE_URL,
        `/api/recipe/bookmarks?folderId=${folderId}&recipeId=${recipeId}`,
        { method: 'DELETE' }
    );
}

/**
 * 레시피 북마크 상태 확인
 */
export async function checkBookmark(
    recipeId: number
): Promise<{ folders: number[]; isBookmarked: boolean }> {
    return await httpJson<{ folders: number[]; isBookmarked: boolean }>(
        BASE_URL,
        `/api/recipe/bookmarks/check/${recipeId}`,
        { method: 'GET' }
    );
}

/**
 * 북마크 이동
 */
export async function moveBookmark(
    bookmarkId: number,
    targetFolderId: number
): Promise<RecipeBookmark> {
    return await httpJson<RecipeBookmark>(
        BASE_URL,
        `/api/recipe/bookmarks/${bookmarkId}/move`,
        {
            method: 'PUT',
            body: JSON.stringify({ targetFolderId })
        }
    );
}
