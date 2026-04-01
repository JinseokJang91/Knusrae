import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { logout as logoutApi } from '@/api/authApi';
import { refreshToken, fetchMemberInfo } from '@/utils/auth';
import type { MemberInfo } from '@/types/auth';

export const useAuthStore = defineStore('auth', () => {
    // State
    const isAuthenticated = ref<boolean>(false);
    const memberInfo = ref<MemberInfo | null>(null);
    const isInitialized = ref<boolean>(false);

    // checkAuth() 중복 호출 방지: 진행 중인 Promise를 공유하여 동시 토큰 갱신 경쟁 조건 방지
    let _checkAuthPromise: Promise<boolean> | null = null;

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
        return memberInfo.value?.isAdmin === true;
    });

    // Actions
    /**
     * Token Refresh를 통해 로그인 상태 확인 및 갱신
     * 앱 초기화 시 호출하여 RefreshToken이 유효한 경우 AccessToken 재발급
     * 동시 호출 시 동일한 Promise를 반환하여 토큰 Rotation 경쟁 조건을 방지
     */
    async function checkAuth(): Promise<boolean> {
        if (_checkAuthPromise) {
            return _checkAuthPromise;
        }
        _checkAuthPromise = (async () => {
            try {
                const success = await refreshToken();
                if (success) {
                    isAuthenticated.value = true;
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
            } finally {
                _checkAuthPromise = null;
            }
        })();
        return _checkAuthPromise;
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
     * isInitialized를 true로 설정하여 이후 라우터 가드의 불필요한 checkAuth() 재호출을 방지
     */
    async function login(): Promise<void> {
        isAuthenticated.value = true;
        isInitialized.value = true;
        await loadMemberInfo();
    }

    /**
     * 로그아웃 처리
     */
    async function logout(): Promise<void> {
        try {
            await logoutApi();
        } catch (error) {
            console.error('로그아웃 API 호출 실패:', error);
        } finally {
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
