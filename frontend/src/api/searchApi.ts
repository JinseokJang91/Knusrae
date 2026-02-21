import { httpJson } from '@/utils/http';
import { getApiBaseUrl } from '@/utils/constants';
import type { Recipe } from '@/types/recipe';

const BASE_URL = getApiBaseUrl('cook');

/**
 * 레시피 제목으로 검색
 * @param keyword 검색어
 * @returns 검색된 레시피 목록
 */
export async function searchRecipes(keyword: string): Promise<Recipe[]> {
    if (!keyword || !keyword.trim()) {
        return [];
    }

    const encodedKeyword = encodeURIComponent(keyword.trim());
    const response = await httpJson<Recipe[] | { data?: Recipe[] }>(BASE_URL, `/api/search/recipes?keyword=${encodedKeyword}`, { method: 'GET' });

    return Array.isArray(response) ? response : (response.data ?? []);
}
