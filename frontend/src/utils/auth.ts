import { httpJson } from '@/utils/http';
import { getApiBaseUrl } from '@/utils/constants';

/**
 * Token Refresh API 호출
 * RefreshToken이 유효한 경우 AccessToken을 재발급받음
 * @returns 성공 시 true, 실패 시 false
 */
export async function refreshToken(): Promise<boolean> {
    try {
        const API_BASE_URL = getApiBaseUrl('auth');
        // httpJson을 사용하지 않는 이유: 401 응답을 정상적인 경우로 처리해야 하므로
        // fetch를 직접 사용하여 response.ok를 체크할 수 있도록 함
        const response = await fetch(`${API_BASE_URL}/api/auth/refresh`, {
            method: 'POST',
            credentials: 'include', // HttpOnly 쿠키 전송
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            return true;
        } else {
            // 401 응답은 정상적인 경우 (로그인하지 않은 상태)이므로 에러로 처리하지 않음
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
        const API_BASE_URL = getApiBaseUrl('member');
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

/**
 * 관리자 이메일 목록
 * 환경 변수로 설정 가능하지만, 기본값은 admin@test.com
 */
const ADMIN_EMAILS = [
    'admin@test.com',
    ...(import.meta.env.VITE_ADMIN_EMAILS ? import.meta.env.VITE_ADMIN_EMAILS.split(',') : [])
].map(email => email.toLowerCase().trim());

/**
 * 이메일이 관리자 이메일인지 확인
 * @param email 확인할 이메일
 * @returns 관리자 이메일이면 true, 아니면 false
 */
export function isAdminEmail(email: string | undefined): boolean {
    if (!email) return false;
    return ADMIN_EMAILS.includes(email.toLowerCase().trim());
}
