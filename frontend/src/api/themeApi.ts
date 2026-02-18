import { httpJson } from '@/utils/http';
import { getApiBaseUrl } from '@/utils/constants';
import type { ThemeCollection, ThemeCollectionDetail } from '@/types/theme';

const BASE_URL = getApiBaseUrl('cook');

/**
 * 활성 테마 목록 조회
 */
export async function getActiveThemes(): Promise<ThemeCollection[]> {
  const response = await httpJson<ThemeCollection[]>(
    BASE_URL,
    '/api/themes/active',
    { method: 'GET' }
  );
  return response;
}

/**
 * 테마별 레시피 목록 조회
 */
export async function getThemeRecipes(
  themeId: number,
  limit: number = 8,
  offset: number = 0
): Promise<ThemeCollectionDetail> {
  const params = new URLSearchParams({
    limit: limit.toString(),
    offset: offset.toString()
  });
  const response = await httpJson<ThemeCollectionDetail>(
    BASE_URL,
    `/api/themes/${themeId}/recipes?${params}`,
    { method: 'GET' }
  );
  return response;
}
