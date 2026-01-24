import { httpJson } from '@/utils/http';
import { getApiBaseUrl } from '@/utils/constants';
import type { PopularRecipeItem } from '@/types/recipe';

const BASE_URL = getApiBaseUrl('cook');

/**
 * 인기 레시피 목록 조회
 * @param limit 조회할 개수 (기본 10)
 * @param period 기간 (24h, 7d, 30d) (기본 24h)
 */
export async function getPopularRecipes(
    limit: number = 10,
    period: '24h' | '7d' | '30d' = '24h'
): Promise<PopularRecipeItem[]> {
    const url = `/api/recipe/popular?limit=${limit}&period=${period}`;
    return await httpJson(BASE_URL, url, {
        method: 'GET'
    });
}

