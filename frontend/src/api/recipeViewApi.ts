// 레시피 조회 기록 API
import { httpJson } from '@/utils/http';
import { getApiBaseUrl } from '@/utils/constants';
import type { RecentViewsResponse, CreateViewResponse } from '@/types/recipe';

const BASE_URL = getApiBaseUrl('cook');

/**
 * 레시피 조회 기록 생성/갱신
 * 
 * @param recipeId 레시피 ID
 * @returns 조회 기록 정보
 */
export async function createRecipeView(recipeId: number): Promise<CreateViewResponse> {
    const url = `/api/recipes/${recipeId}/view`;
    const response = await httpJson(BASE_URL, url, {
        method: 'POST'
    });
    return response.data;
}

/**
 * 최근 본 레시피 목록 조회
 * 
 * @param memberId 회원 ID
 * @param limit 조회할 개수 (기본값: 10)
 * @param offset 건너뛸 개수 (기본값: 0)
 * @returns 최근 본 레시피 목록
 */
export async function getRecentViews(
    memberId: number,
    limit: number = 10,
    offset: number = 0
): Promise<RecentViewsResponse> {
    const params = new URLSearchParams({
        limit: limit.toString(),
        offset: offset.toString()
    });

    const url = `/api/members/${memberId}/recent-views?${params}`;
    const response = await httpJson(
        BASE_URL,
        url,
        {
            method: 'GET'
        }
    );
    return response.data;
}

/**
 * 조회 기록 전체 삭제
 * 
 * @param memberId 회원 ID
 */
export async function deleteAllRecentViews(memberId: number): Promise<void> {
    const url = `/api/members/${memberId}/recent-views`;
    await httpJson(
        BASE_URL,
        url,
        {
            method: 'DELETE'
        }
    );
}
