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
