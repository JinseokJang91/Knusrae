import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { refreshToken, fetchMemberInfo, isAdminEmail } from '@/utils/auth';
import { getApiBaseUrl } from '@/utils/constants';
import type { MemberInfo } from '@/types/auth';

export const useAuthStore = defineStore('auth', () => {
    // State
    const isAuthenticated = ref<boolean>(false);
    const memberInfo = ref<MemberInfo | null>(null);
    const isInitialized = ref<boolean>(false);

    // Getters
    const isLoggedIn = computed(() => isAuthenticated.value);
    const memberName = computed(() => {
        if (!memberInfo.value) return '';
        return memberInfo.value.nickname || memberInfo.value.name || '사용자';
    });

    const memberProfileImage = computed(() => {
        if (!memberInfo.value) return '';
        return memberInfo.value.profileImage || '';
    });

    const isAdmin = computed(() => {
        if (!memberInfo.value || !memberInfo.value.email) return false;
        return isAdminEmail(memberInfo.value.email);
    });

    // Actions
    /**
     * Token Refresh를 통해 로그인 상태 확인 및 갱신
     * 앱 초기화 시 호출하여 RefreshToken이 유효한 경우 AccessToken 재발급
     */
    async function checkAuth(): Promise<boolean> {
        try {
            const success = await refreshToken();
            if (success) {
                isAuthenticated.value = true;
                // 사용자 정보도 함께 조회
                await loadMemberInfo();
            } else {
                isAuthenticated.value = false;
                memberInfo.value = null;
            }
            isInitialized.value = true;
            return success;
        } catch (error) {
            console.error('인증 확인 실패:', error);
            isAuthenticated.value = false;
            memberInfo.value = null;
            isInitialized.value = true;
            return false;
        }
    }

    /**
     * 사용자 정보 로드
     */
    async function loadMemberInfo(): Promise<void> {
        try {
            const info = await fetchMemberInfo();
            if (info) {
                memberInfo.value = info;
            } else {
                memberInfo.value = null;
            }
        } catch (error) {
            console.error('사용자 정보 로드 실패:', error);
            memberInfo.value = null;
        }
    }

    /**
     * 로그인 성공 시 호출
     * Token은 HttpOnly Cookie로 저장되므로 별도 처리 불필요
     */
    async function login(): Promise<void> {
        isAuthenticated.value = true;
        await loadMemberInfo();
    }

    /**
     * 로그아웃 처리
     */
    async function logout(): Promise<void> {
        try {
            const API_BASE_URL = getApiBaseUrl('auth');
            await fetch(`${API_BASE_URL}/api/auth/logout`, {
                method: 'POST',
                credentials: 'include'
            });
        } catch (error) {
            console.error('로그아웃 API 호출 실패:', error);
        } finally {
            // 상태 초기화
            isAuthenticated.value = false;
            memberInfo.value = null;
        }
    }

    /**
     * 상태 초기화 (테스트용)
     */
    function reset(): void {
        isAuthenticated.value = false;
        memberInfo.value = null;
        isInitialized.value = false;
    }

    return {
        // State
        isAuthenticated,
        memberInfo,
        isInitialized,
        // Getters
        isLoggedIn,
        memberName,
        memberProfileImage,
        isAdmin,
        // Actions
        checkAuth,
        loadMemberInfo,
        login,
        logout,
        reset
    };
});

