import { httpJson } from '@/utils/http';
import { getApiBaseUrl } from '@/utils/constants';
import type { TrendingCategoriesResponse, CategoryRecipesResponse } from '@/types/category';
import { EmptySuccessfulJsonBodyError } from '@/utils/errorHandler';

const BASE_URL = getApiBaseUrl('cook');

/**
 * 트렌딩 카테고리 조회
 * @param limit 조회할 카테고리 개수 (기본 2)
 * @param period 기간 (7d, 30d) (기본 30d)
 */
export async function getTrendingCategories(limit: number = 2, period: '7d' | '30d' = '30d'): Promise<TrendingCategoriesResponse> {
    const url = `/api/cook/categories/trending?limit=${limit}&period=${period}`;
    const data = await httpJson<TrendingCategoriesResponse | null>(BASE_URL, url, {
        method: 'GET'
    });
    return {
        categories: data?.categories ?? [],
        period: data?.period ?? period
    };
}

/**
 * 카테고리별 레시피 조회
 * @param codeId 코드 ID
 * @param detailCodeId 상세 코드 ID
 * @param limit 조회할 레시피 개수 (기본 12)
 * @param sort 정렬 기준 (latest, popular, mixed) (기본 mixed)
 */
export async function getCategoryRecipes(codeId: string, detailCodeId: string, limit: number = 12, sort: 'latest' | 'popular' | 'mixed' = 'mixed'): Promise<CategoryRecipesResponse> {
    const url = `/api/cook/categories/${codeId}/${detailCodeId}/recipes?limit=${limit}&sort=${sort}`;
    const data = await httpJson<CategoryRecipesResponse | null>(BASE_URL, url, {
        method: 'GET'
    });
    if (data == null) {
        throw new EmptySuccessfulJsonBodyError();
    }
    return {
        category: data.category,
        recipes: data.recipes ?? [],
        totalCount: data.totalCount ?? 0
    };
}
