import { getApiBaseUrl } from '@/utils/constants';

const BASE_URL = getApiBaseUrl('auth');

/** 테스트 계정 (개발용 API 응답) */
export interface TestAccount {
    id?: number;
    email: string;
    name?: string;
    socialRole?: string;
}

/**
 * Token Refresh
 * RefreshToken이 유효한 경우 AccessToken 재발급. 401은 미로그인으로 정상 처리.
 * @returns 성공 시 true, 실패(401 등) 시 false
 */
export async function refreshToken(): Promise<boolean> {
    try {
        const response = await fetch(`${BASE_URL}/api/auth/refresh`, {
            method: 'POST',
            credentials: 'include',
            headers: { 'Content-Type': 'application/json' }
        });
        return response.ok;
    } catch (error) {
        console.error('Token Refresh 실패:', error);
        return false;
    }
}

/**
 * 로그아웃
 */
export async function logout(): Promise<void> {
    await fetch(`${BASE_URL}/api/auth/logout`, {
        method: 'POST',
        credentials: 'include'
    });
}

/** 프로덕션 빌드에서는 테스트 API 호출 불가 (보안·운영 정책) */
function assertDevOnly(): void {
    if (import.meta.env.PROD) {
        throw new Error('테스트 계정 API는 개발 환경에서만 사용할 수 있습니다.');
    }
}

/**
 * 테스트 계정 목록 조회 (개발용, 프로덕션에서는 호출 불가)
 */
export async function getTestAccounts(): Promise<TestAccount[]> {
    assertDevOnly();
    const response = await fetch(`${BASE_URL}/api/auth/test/accounts`, {
        method: 'GET',
        credentials: 'include'
    });
    if (!response.ok) {
        throw new Error('테스트 계정 목록을 가져오는데 실패했습니다.');
    }
    return await response.json();
}

/** testLogin 결과 */
export interface TestLoginResult {
    success: boolean;
    message?: string;
}

/**
 * 테스트 계정으로 로그인 (개발용, 프로덕션에서는 호출 불가)
 */
export async function testLogin(email: string): Promise<TestLoginResult> {
    assertDevOnly();
    const response = await fetch(`${BASE_URL}/api/auth/test/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({ email })
    });
    if (response.ok) {
        return { success: true };
    }
    const body = await response.json().catch(() => ({}));
    return {
        success: false,
        message: (body as { message?: string }).message || '알 수 없는 오류'
    };
}
