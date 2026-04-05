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
 * 운영 빌드에서 VITE_API_BASE_URL_* 가 비어 있으면 localhost로 떨어져
 * (https 사이트 → http localhost) CORS/혼합 콘텐츠 오류가 납니다.
 * CI Variables 누락·캐시된 구번들 등에 대비한 프로덕션 기본 API 호스트입니다.
 */
const PROD_API_BASE_FALLBACK = 'https://api.knusrae.com';

/**
 * API Base URL 가져오기
 * - 우선 해당 서비스용 VITE_API_BASE_URL_* (빌드 시 주입)
 * - 프로덕션 빌드인데 비어 있으면 PROD_API_BASE_FALLBACK (로컬호스트 사용 안 함)
 * - 개발 모드에서만 서비스별 localhost 포트 기본값
 */
export function getApiBaseUrl(service: 'auth' | 'member' | 'cook' = 'auth'): string {
    const envKey = service === 'auth' ? 'VITE_API_BASE_URL_AUTH' : service === 'member' ? 'VITE_API_BASE_URL_MEMBER' : 'VITE_API_BASE_URL_COOK';

    const fromEnv = import.meta.env[envKey as keyof ImportMetaEnv] as string | undefined;
    const trimmed = typeof fromEnv === 'string' ? fromEnv.trim() : '';
    if (trimmed !== '') {
        return trimmed;
    }

    if (import.meta.env.PROD) {
        return PROD_API_BASE_FALLBACK;
    }

    const devDefault = service === 'auth' ? 'http://localhost:8081' : service === 'member' ? 'http://localhost:8083' : 'http://localhost:8082';
    return devDefault;
}
