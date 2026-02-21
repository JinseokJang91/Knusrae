import { refreshToken as refreshTokenFromApi } from '@/api/authApi';
import { getMemberMe } from '@/api/memberApi';
import type { MemberInfo } from '@/types/auth';

/**
 * Token Refresh API 호출
 * RefreshToken이 유효한 경우 AccessToken을 재발급받음
 * @returns 성공 시 true, 실패 시 false
 */
export async function refreshToken(): Promise<boolean> {
    return await refreshTokenFromApi();
}

export async function fetchMemberInfo(): Promise<MemberInfo | null> {
    return await getMemberMe();
}

/**
 * 관리자 이메일 목록
 * 환경 변수로 설정 가능하지만, 기본값은 admin@test.com
 */
const ADMIN_EMAILS = ['admin@test.com', ...(import.meta.env.VITE_ADMIN_EMAILS ? import.meta.env.VITE_ADMIN_EMAILS.split(',') : [])].map((email) => email.toLowerCase().trim());

/**
 * 이메일이 관리자 이메일인지 확인
 * @param email 확인할 이메일
 * @returns 관리자 이메일이면 true, 아니면 false
 */
export function isAdminEmail(email: string | undefined): boolean {
    if (!email) return false;
    return ADMIN_EMAILS.includes(email.toLowerCase().trim());
}
