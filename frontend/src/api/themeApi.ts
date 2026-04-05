import { httpJson } from '@/utils/http';
import { getApiBaseUrl } from '@/utils/constants';
import type { ThemeCollection, ThemeCollectionDetail } from '@/types/theme';

const BASE_URL = getApiBaseUrl('cook');

/**
 * 활성 테마 목록 조회
 */
export async function getActiveThemes(): Promise<ThemeCollection[]> {
    const data = await httpJson<ThemeCollection[] | null>(BASE_URL, '/api/themes/active', { method: 'GET' });
    return Array.isArray(data) ? data : [];
}

/**
 * 테마별 레시피 목록 조회
 */
export async function getThemeRecipes(themeId: number, limit: number = 8, offset: number = 0): Promise<ThemeCollectionDetail | null> {
    const params = new URLSearchParams({
        limit: limit.toString(),
        offset: offset.toString()
    });
    return await httpJson<ThemeCollectionDetail | null>(BASE_URL, `/api/themes/${themeId}/recipes?${params}`, { method: 'GET' });
}
