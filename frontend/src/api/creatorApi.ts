import { httpJson } from '@/utils/http';
import { getApiBaseUrl } from '@/utils/constants';
import type { Creator } from '@/types/creator';

const BASE_URL = getApiBaseUrl('cook');

/**
 * 추천 크리에이터 목록 조회
 */
export async function getRecommendedCreators(limit: number = 6): Promise<Creator[]> {
    const data = await httpJson<Creator[] | null>(BASE_URL, `/api/cook/creators/recommended?limit=${limit}`, { method: 'GET' });
    return Array.isArray(data) ? data : [];
}
