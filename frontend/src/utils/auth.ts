import { httpJson } from '@/utils/http';

/**
 * Token Refresh API 호출
 * RefreshToken이 유효한 경우 AccessToken을 재발급받음
 * @returns 성공 시 true, 실패 시 false
 */
export async function refreshToken(): Promise<boolean> {
    try {
        
        const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081';
        const response = await fetch(`${API_BASE_URL}/api/auth/refresh`, {
            method: 'POST',
            credentials: 'include' // HttpOnly 쿠키 전송
        });

        if (response.ok) {
            return true;
        } else {
            // 401 응답은 정상적인 경우 (로그인하지 않은 상태)이므로 에러로 처리하지 않음
            // 개발 환경에서만 로그 출력
            if (import.meta.env.DEV && response.status === 401) {
                const errorData = await response.json().catch(() => ({}));
                console.log('Refresh Token이 없습니다. (로그인하지 않은 상태)', errorData);
            }
            return false;
        }
    } catch (error) {
        // 네트워크 에러나 기타 예외만 catch 블록으로 들어옴
        console.error('Token Refresh 실패:', error);
        return false;
    }
}

export async function fetchMemberInfo(): Promise<any> {
    try {
        // member-service의 BASE URL 사용 (환경 변수가 있으면 사용, 없으면 기본값)
        const API_BASE_URL = import.meta.env.VITE_API_BASE_URL_MEMBER;
        const memberInfo = await httpJson(API_BASE_URL, '/api/member/me', {
            method: 'GET'
        });

        return memberInfo;
    } catch (error) {
        console.error('사용자 정보 조회 실패:', error);
        // API 호출 실패 시 기본값 설정
        return null;
    }
}
