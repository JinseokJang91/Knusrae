/**
 * 공통 상수 정의
 */

/**
 * 가이드 이미지 경로 매핑
 */
export const GUIDE_IMAGES: Record<string, string> = {
    basic: '/guide/Guide_01.png',
    ingredients: '/guide/Guide_02.png',
    classification: '/guide/Guide_03.png',
    steps: '/guide/Guide_04.png',
    settings: '/guide/Guide_05.png'
};

/**
 * API Base URL 가져오기
 * 환경 변수가 없으면 기본값 사용
 */
export function getApiBaseUrl(service: 'auth' | 'member' | 'cook' = 'auth'): string {
    const envKey = service === 'auth' 
        ? 'VITE_API_BASE_URL_AUTH' 
        : service === 'member' 
        ? 'VITE_API_BASE_URL_MEMBER' 
        : 'VITE_API_BASE_URL_COOK';
    
    const defaultUrl = service === 'auth'
        ? 'http://localhost:8081'
        : service === 'member'
        ? 'http://localhost:8083'
        : 'http://localhost:8082';
    
    return import.meta.env[envKey] || defaultUrl;
}

