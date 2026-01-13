import { httpJson } from './http';
import { getApiBaseUrl } from './constants';
import type { Recipe } from '@/types/recipe';

/**
 * 검색 API 서비스
 * 여러 도메인(레시피 등)의 검색 기능을 제공
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
 * localStorage 키 생성 (계정별로 분리)
 */
const getStorageKey = (memberId?: number): string => {
    if (memberId) {
        return `recent_search_keywords_${memberId}`;
    }
    return 'recent_search_keywords_guest';
};

/**
 * 최대 저장 개수
 */
const MAX_RECENT_SEARCH_COUNT = 10;

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
 * 최근 검색어 목록 조회 (localStorage 기반)
 * 
 * @param memberId 회원 ID (선택적)
 * @returns 최근 검색어 목록 (최신순)
 */
export function getRecentSearchKeywords(memberId?: number): RecentSearchKeyword[] {
    try {
        const key = getStorageKey(memberId);
        const stored = localStorage.getItem(key);
        
        if (!stored) {
            return [];
        }
        
        const keywords: RecentSearchKeyword[] = JSON.parse(stored);
        // 최신순으로 정렬 (createdAt 기준)
        return keywords.sort((a, b) => {
            const dateA = new Date(a.createdAt).getTime();
            const dateB = new Date(b.createdAt).getTime();
            return dateB - dateA;
        });
    } catch (error) {
        console.error('최근 검색어 조회 중 오류 발생:', error);
        return [];
    }
}

/**
 * 검색어 저장 (localStorage 기반)
 * 
 * @param keyword 검색어
 * @param memberId 회원 ID (선택적)
 */
export function saveRecentSearchKeyword(keyword: string, memberId?: number): void {
    if (!keyword || !keyword.trim()) {
        return;
    }

    try {
        const key = getStorageKey(memberId);
        const trimmedKeyword = keyword.trim();
        
        // 기존 목록 가져오기
        let keywords = getRecentSearchKeywords(memberId);
        
        // 중복 검색어 제거 (같은 검색어가 있으면 삭제)
        keywords = keywords.filter(k => k.keyword !== trimmedKeyword);
        
        // 새 검색어 추가 (최신순 유지)
        const newKeyword: RecentSearchKeyword = {
            id: Date.now(), // 고유 ID 생성
            keyword: trimmedKeyword,
            createdAt: new Date().toISOString()
        };
        
        keywords.unshift(newKeyword);
        
        // 최대 개수 제한
        if (keywords.length > MAX_RECENT_SEARCH_COUNT) {
            keywords = keywords.slice(0, MAX_RECENT_SEARCH_COUNT);
        }
        
        // localStorage에 저장
        localStorage.setItem(key, JSON.stringify(keywords));
    } catch (error) {
        console.error('검색어 저장 중 오류 발생:', error);
    }
}

/**
 * 검색어 삭제 (localStorage 기반)
 * 
 * @param keywordId 검색어 ID
 * @param memberId 회원 ID (선택적)
 */
export function deleteRecentSearchKeyword(keywordId: number, memberId?: number): void {
    try {
        const key = getStorageKey(memberId);
        let keywords = getRecentSearchKeywords(memberId);
        
        // 해당 ID의 검색어 제거
        keywords = keywords.filter(k => k.id !== keywordId);
        
        // localStorage에 저장
        localStorage.setItem(key, JSON.stringify(keywords));
    } catch (error) {
        console.error('검색어 삭제 중 오류 발생:', error);
        throw error;
    }
}

/**
 * 모든 최근 검색어 삭제 (localStorage 기반)
 * 
 * @param memberId 회원 ID (선택적)
 */
export function deleteAllRecentSearchKeywords(memberId?: number): void {
    try {
        const key = getStorageKey(memberId);
        localStorage.removeItem(key);
    } catch (error) {
        console.error('전체 검색어 삭제 중 오류 발생:', error);
        throw error;
    }
}

