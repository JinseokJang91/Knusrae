import { httpJson, httpForm } from '@/utils/http';
import { getApiBaseUrl } from '@/utils/constants';
import type { MemberInfo } from '@/types/auth';

const BASE_URL = getApiBaseUrl('member');

/**
 * 현재 로그인한 회원 정보 조회
 * @returns 회원 정보, 실패 시 null
 */
export async function getMemberMe(): Promise<MemberInfo | null> {
    try {
        const memberInfo = await httpJson<MemberInfo>(BASE_URL, '/api/member/me', {
            method: 'GET'
        });
        return memberInfo;
    } catch (error) {
        console.error('사용자 정보 조회 실패:', error);
        return null;
    }
}

/**
 * 회원 프로필 수정 (이름, 닉네임, 자기소개, 프로필 이미지)
 */
export async function updateProfile(formData: FormData): Promise<void> {
    await httpForm(BASE_URL, '/api/member/profile', formData, {
        method: 'PUT'
    });
}
