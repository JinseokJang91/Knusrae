import { httpJson } from './http';
import { getApiBaseUrl } from './constants';
import type { Recipe } from '@/types/recipe';

/**
 * 검색 API 서비스
 * 여러 도메인(레시피, 게시판 등)의 검색 기능을 제공
 */

const API_BASE_URL = getApiBaseUrl('cook');

/**
 * 레시피 제목으로 검색
 * 
 * @param keyword 검색어
 * @returns 검색된 레시피 목록
 * @throws Error API 호출 실패 시
 */
export async function searchRecipes(keyword: string): Promise<Recipe[]> {
    if (!keyword || !keyword.trim()) {
        return [];
    }

    try {
        const encodedKeyword = encodeURIComponent(keyword.trim());
        const response = await httpJson(
            API_BASE_URL,
            `/api/search/recipes?keyword=${encodedKeyword}`,
            {
                method: 'GET'
            }
        );

        // 응답이 배열인지 확인
        const data = Array.isArray(response) ? response : (response.data || []);
        return data;
    } catch (error) {
        console.error('레시피 검색 중 오류 발생:', error);
        throw error;
    }
}

