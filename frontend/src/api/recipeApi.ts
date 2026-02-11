import { httpJson, httpForm } from '@/utils/http';
import { getApiBaseUrl } from '@/utils/constants';
import type {
    PopularRecipeItem,
    Recipe,
    RecipeDetail,
    RecipeComment,
    FavoriteItem,
    MyCommentItem
} from '@/types/recipe';

const BASE_URL = getApiBaseUrl('cook');

/**
 * 인기 레시피 목록 조회
 */
export async function getPopularRecipes(
    limit: number = 10,
    period: '24h' | '7d' | '30d' = '24h'
): Promise<PopularRecipeItem[]> {
    const url = `/api/recipe/popular?limit=${limit}&period=${period}`;
    return await httpJson(BASE_URL, url, { method: 'GET' });
}

/**
 * 레시피 상세 조회
 */
export async function getRecipeDetail(recipeId: number): Promise<RecipeDetail> {
    return await httpJson<RecipeDetail>(BASE_URL, `/api/recipe/${recipeId}`, { method: 'GET' });
}

/**
 * 전체 레시피 목록 조회
 */
export async function getRecipeListAll(): Promise<Recipe[]> {
    const response = await httpJson<Recipe[] | { data?: Recipe[] }>(
        BASE_URL,
        '/api/recipe/list/all',
        { method: 'GET' }
    );
    return Array.isArray(response) ? response : response.data ?? [];
}

/**
 * 회원별 레시피 목록 조회
 */
export async function getRecipeListByMember(memberId: number): Promise<Recipe[]> {
    return await httpJson<Recipe[]>(BASE_URL, `/api/recipe/list/member/${memberId}`, {
        method: 'GET'
    });
}

/**
 * 레시피 등록 (FormData)
 */
export async function createRecipe(formData: FormData): Promise<void> {
    await httpForm(BASE_URL, '/api/recipe', formData, { method: 'POST' });
}

/**
 * 레시피 수정 (FormData)
 */
export async function updateRecipe(recipeId: number, formData: FormData): Promise<void> {
    await httpForm(BASE_URL, `/api/recipe/${recipeId}`, formData, { method: 'PUT' });
}

/**
 * 레시피 삭제
 */
export async function deleteRecipe(recipeId: number): Promise<void> {
    await httpJson(BASE_URL, `/api/recipe/${recipeId}`, { method: 'DELETE' });
}

/**
 * 찜 목록 조회
 */
export async function getFavorites(memberId: number): Promise<FavoriteItem[]> {
    const response = await httpJson<FavoriteItem[] | { data?: FavoriteItem[] }>(
        BASE_URL,
        `/api/recipe/favorites/${memberId}`,
        { method: 'GET' }
    );
    return Array.isArray(response) ? response : response.data ?? [];
}

/**
 * 찜 여부 확인
 */
export async function checkFavorite(
    memberId: number,
    recipeId: number
): Promise<{ isFavorite: boolean }> {
    return await httpJson<{ isFavorite: boolean }>(
        BASE_URL,
        `/api/recipe/favorites/check?memberId=${memberId}&recipeId=${recipeId}`,
        { method: 'GET' }
    );
}

/**
 * 찜 토글
 */
export async function toggleFavorite(
    memberId: number,
    recipeId: number
): Promise<{ isFavorite: boolean }> {
    return await httpJson<{ isFavorite: boolean }>(
        BASE_URL,
        `/api/recipe/favorites/toggle?memberId=${memberId}&recipeId=${recipeId}`,
        { method: 'PUT' }
    );
}

/**
 * 찜 해제
 */
export async function removeFavorite(memberId: number, recipeId: number): Promise<void> {
    await httpJson(
        BASE_URL,
        `/api/recipe/favorites?memberId=${memberId}&recipeId=${recipeId}`,
        { method: 'DELETE' }
    );
}

/** 댓글 페이징 응답 */
export interface RecipeCommentsPageResponse {
    comments?: RecipeComment[];
    currentPage: number;
    totalPages: number;
    totalComments: number;
}

/** 내 댓글 목록 페이징 응답 */
export interface MyCommentsPageResponse {
    content: MyCommentItem[];
    currentPage: number;
    totalPages: number;
    totalElements: number;
    hasNext?: boolean;
    hasPrevious?: boolean;
}

/**
 * 내 댓글 목록 조회 (페이징, 레시피 요약 포함)
 */
export async function getMyComments(
    memberId: number,
    page: number,
    size: number
): Promise<MyCommentsPageResponse> {
    return await httpJson<MyCommentsPageResponse>(
        BASE_URL,
        `/api/recipe/comments/member/${memberId}?page=${page}&size=${size}`,
        { method: 'GET' }
    );
}

/**
 * 댓글 목록 조회 (페이징)
 */
export async function getComments(
    recipeId: number,
    page: number,
    size: number
): Promise<RecipeCommentsPageResponse> {
    return await httpJson<RecipeCommentsPageResponse>(
        BASE_URL,
        `/api/recipe/comments/${recipeId}/page?page=${page}&size=${size}`,
        { method: 'GET' }
    );
}

/**
 * 댓글 작성 (JSON)
 */
export async function createComment(
    recipeId: number,
    body: { memberId: number; content: string; parentId?: number | null }
): Promise<void> {
    await httpJson(BASE_URL, `/api/recipe/comments/${recipeId}`, {
        method: 'POST',
        body: JSON.stringify(body)
    });
}

/**
 * 댓글 작성 (이미지 포함 FormData)
 */
export async function createCommentWithImage(
    recipeId: number,
    formData: FormData
): Promise<void> {
    await httpForm(BASE_URL, `/api/recipe/comments/${recipeId}/with-image`, formData, {
        method: 'POST'
    });
}

/**
 * 댓글 수정 (JSON)
 */
export async function updateComment(
    commentId: number,
    body: { memberId: number; content: string }
): Promise<void> {
    await httpJson(BASE_URL, `/api/recipe/comments/${commentId}`, {
        method: 'PUT',
        body: JSON.stringify(body)
    });
}

/**
 * 댓글 수정 (이미지 포함 FormData)
 */
export async function updateCommentWithImage(
    commentId: number,
    formData: FormData
): Promise<void> {
    await httpForm(
        BASE_URL,
        `/api/recipe/comments/${commentId}/with-image`,
        formData,
        { method: 'PUT' }
    );
}

/**
 * 댓글 삭제
 */
export async function deleteComment(commentId: number, memberId: number): Promise<void> {
    await httpJson(BASE_URL, `/api/recipe/comments/${commentId}?memberId=${memberId}`, {
        method: 'DELETE'
    });
}
