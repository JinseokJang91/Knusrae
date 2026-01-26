// 레시피 추천 API
import { httpJson } from '@/utils/http';
import { getApiBaseUrl } from '@/utils/constants';
import type { TodayRecommendationsResponse } from '@/types/recipe';

const BASE_URL = getApiBaseUrl('cook');

/**
 * 오늘의 레시피 추천 조회
 * 
 * @param limit 반환할 레시피 개수 (기본값: 3)
 * @param refresh 캐시 무시 여부 (기본값: false)
 * @returns 오늘의 추천 레시피 목록
 */
export async function getTodayRecommendations(
    limit: number = 3,
    refresh: boolean = false
): Promise<TodayRecommendationsResponse> {
    const params = new URLSearchParams({
        limit: limit.toString(),
        refresh: refresh.toString()
    });

    const url = `/api/recipes/recommendations/today?${params}`;
    const response = await httpJson(BASE_URL, url, {
        method: 'GET'
    });
    return response.data;
}
