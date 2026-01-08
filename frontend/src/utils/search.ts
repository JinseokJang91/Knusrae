import { httpJson } from './http';
import { getApiBaseUrl } from './constants';
import type { Recipe } from '@/types/recipe';

/**
 * 검색 API 서비스
 * 여러 도메인(레시피, 게시판 등)의 검색 기능을 제공
 */

const API_BASE_URL = getApiBaseUrl('cook');

/**
 * 최근 검색어 타입
 */
export interface RecentSearchKeyword {
    id: number;
    keyword: string;
    createdAt: string;
}

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

/**
 * 최근 검색어 목록 조회
 * 
 * @returns 최근 검색어 목록 (최신순)
 * @throws Error API 호출 실패 시
 */
export async function getRecentSearchKeywords(): Promise<RecentSearchKeyword[]> {
    try {
        const response = await httpJson(
            API_BASE_URL,
            '/api/search/recent',
            {
                method: 'GET'
            }
        );

        // 응답이 배열인지 확인
        const data = Array.isArray(response) ? response : (response.data || []);
        return data;
    } catch (error) {
        console.error('최근 검색어 조회 중 오류 발생:', error);
        // 인증되지 않은 사용자는 빈 배열 반환
        return [];
    }
}

/**
 * 검색어 저장
 * 
 * @param keyword 검색어
 * @throws Error API 호출 실패 시
 */
export async function saveRecentSearchKeyword(keyword: string): Promise<void> {
    if (!keyword || !keyword.trim()) {
        return;
    }

    try {
        const encodedKeyword = encodeURIComponent(keyword.trim());
        await httpJson(
            API_BASE_URL,
            `/api/search/recent?keyword=${encodedKeyword}`,
            {
                method: 'POST'
            }
        );
    } catch (error) {
        console.error('검색어 저장 중 오류 발생:', error);
        // 인증되지 않은 사용자는 무시
    }
}

/**
 * 검색어 삭제
 * 
 * @param keywordId 검색어 ID
 * @throws Error API 호출 실패 시
 */
export async function deleteRecentSearchKeyword(keywordId: number): Promise<void> {
    try {
        await httpJson(
            API_BASE_URL,
            `/api/search/recent/${keywordId}`,
            {
                method: 'DELETE'
            }
        );
    } catch (error) {
        console.error('검색어 삭제 중 오류 발생:', error);
        throw error;
    }
}

/**
 * 모든 최근 검색어 삭제
 * 
 * @throws Error API 호출 실패 시
 */
export async function deleteAllRecentSearchKeywords(): Promise<void> {
    try {
        await httpJson(
            API_BASE_URL,
            '/api/search/recent',
            {
                method: 'DELETE'
            }
        );
    } catch (error) {
        console.error('전체 검색어 삭제 중 오류 발생:', error);
        throw error;
    }
}

