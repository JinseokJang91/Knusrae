<script setup lang="ts">
import logoImage from '@/assets/images/logo/logo-icon.png';
import SocialLoginButtons from '@/components/SocialLoginButtons.vue';
import { openOAuthPopup } from '@/utils/oauth';
import { ref } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';
import { useAppToast } from '@/utils/toast';
import { getTestAccounts, testLogin, type TestAccount } from '@/api/authApi';

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();
const { showSuccess, showError } = useAppToast();

const isDevelopment = import.meta.env.DEV;
const showTestAccounts = ref(false);
const testAccounts = ref<TestAccount[]>([]);
const loadingTestAccounts = ref(false);

// redirect 경로 가져오기 (쿼리 파라미터 또는 기본값)
const getRedirectPath = (): string => {
    const redirect = route.query.redirect as string;
    return redirect || '/';
};

// 로그인 후 이동할 경로로 리다이렉트
function redirectAfterLogin() {
    const redirectPath = getRedirectPath();
    router.push(redirectPath);
}

// 소셜 로그인 핸들러 (SocialLoginButtons 컴포넌트에서 emit된 provider에 따라 처리)
function handleSocialLogin(provider: 'kakao' | 'google' | 'naver') {
    // redirect 경로를 localStorage에 저장 (OAuth 팝업에서 사용)
    const redirectPath = getRedirectPath();
    localStorage.setItem('oauth_redirect', redirectPath);

    switch (provider) {
        case 'naver':
            openOAuthPopup('naver', import.meta.env.VITE_NAVER_CLIENT_ID, import.meta.env.VITE_NAVER_REDIRECT_URI);
            break;
        case 'google':
            openOAuthPopup('google', import.meta.env.VITE_GOOGLE_CLIENT_ID, import.meta.env.VITE_GOOGLE_REDIRECT_URI, { scope: 'openid email profile' });
            break;
        case 'kakao':
            openOAuthPopup('kakao', import.meta.env.VITE_KAKAO_CLIENT_ID, import.meta.env.VITE_KAKAO_REDIRECT_URI);
            break;
    }
}

function goHome() {
    router.push('/');
}

function goBack() {
    router.go(-1);
}

async function loadTestAccounts() {
    if (!isDevelopment) return;

    loadingTestAccounts.value = true;
    try {
        testAccounts.value = await getTestAccounts();
        showTestAccounts.value = true;
    } catch (error) {
        console.error('테스트 계정 로드 에러:', error);
        showError('테스트 계정 목록을 가져오는 중 오류가 발생했습니다.');
    } finally {
        loadingTestAccounts.value = false;
    }
}

async function loginWithTestAccount(memberId: number) {
    try {
        const result = await testLogin(memberId);
        if (result.success) {
            showSuccess('테스트 계정으로 로그인되었습니다!');
            await authStore.login();
            redirectAfterLogin();
        } else {
            showError(`로그인 실패: ${result.message || '알 수 없는 오류'}`);
        }
    } catch (error) {
        console.error('테스트 로그인 에러:', error);
        showError('로그인 중 오류가 발생했습니다.');
    }
}

// 관리자 로그인 (테스트 계정 목록에서 이름이 admin인 계정으로 로그인)
async function loginAsAdmin() {
    const accounts = await getTestAccounts();
    const admin = accounts.find((a) => (a.name || '').toLowerCase().includes('admin'));
    if (admin?.id != null) {
        await loginWithTestAccount(admin.id);
    } else {
        showError('관리자 테스트 계정을 찾을 수 없습니다.');
    }
}
</script>

<template>
    <div class="bg-white dark:bg-gray-950 flex items-center justify-center min-h-screen min-w-[100vw] overflow-hidden relative">
        <!-- 홈 아이콘 -->
        <div class="absolute top-6 left-6 z-10">
            <Button @click="goHome" icon="pi pi-home" size="large" rounded title="홈으로 돌아가기" />
        </div>

        <!-- 뒤로가기 버튼 -->
        <div class="absolute top-6 left-20 z-10">
            <Button @click="goBack" icon="pi pi-arrow-left" size="large" rounded title="이전 페이지로 돌아가기" />
        </div>

        <div class="flex flex-col items-center justify-center">
            <div style="border-radius: 56px; padding: 0.4em; background: linear-gradient(180deg, var(--primary-color) 10%, rgba(33, 150, 243, 0) 50%)">
                <div class="w-full bg-white dark:bg-gray-900 py-20 px-8 sm:px-40" style="border-radius: 53px">
                    <div class="text-center mb-8">
                        <img :src="logoImage" alt="로고 이미지" class="logo-image w-48 h-auto mx-auto" />
                        <span class="text-2xl font-bold">로그인 / 회원가입</span>
                    </div>

                    <div class="space-y-4">
                        <SocialLoginButtons @click="handleSocialLogin" />

                        <!-- 개발 환경에서만 표시되는 테스트 계정 로그인 -->
                        <div v-if="isDevelopment" class="mt-8 pt-6 border-t border-gray-300 dark:border-gray-700">
                            <!-- 관리자 로그인 버튼 -->
                            <button
                                @click="loginAsAdmin"
                                class="w-full flex items-center justify-center gap-2 bg-red-100 dark:bg-red-900 border border-red-300 dark:border-red-700 rounded-lg px-6 py-3 text-red-700 dark:text-red-300 hover:bg-red-200 dark:hover:bg-red-800 transition-colors mb-4"
                            >
                                <i class="pi pi-shield text-lg"></i>
                                <span class="font-medium">관리자 로그인</span>
                            </button>

                            <button
                                @click="loadTestAccounts"
                                :disabled="loadingTestAccounts"
                                class="w-full flex items-center justify-center gap-2 bg-gray-100 dark:bg-gray-900 border border-gray-300 dark:border-gray-700 rounded-lg px-6 py-3 text-gray-700 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-800 transition-colors disabled:opacity-50"
                            >
                                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
                                </svg>
                                <span class="font-medium">{{ loadingTestAccounts ? '로딩 중...' : '테스트 계정으로 로그인' }}</span>
                            </button>

                            <!-- 테스트 계정 목록 -->
                            <div v-if="showTestAccounts" class="mt-4 space-y-2">
                                <div
                                    v-for="account in testAccounts"
                                    :key="account.id"
                                    @click="account.id != null && loginWithTestAccount(account.id)"
                                    class="p-3 bg-gray-100 dark:bg-gray-800 rounded-lg hover:bg-gray-200 dark:hover:bg-gray-700 transition-colors cursor-pointer"
                                >
                                    <div class="flex items-center justify-between">
                                        <div class="flex-1">
                                            <div class="font-medium text-gray-900 dark:text-gray-100">{{ account.name }}</div>
                                            <div class="text-sm text-gray-600 dark:text-gray-400">{{ account.email }}</div>
                                        </div>
                                        <div
                                            class="text-xs px-2 py-1 rounded"
                                            :class="{
                                                'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200': account.socialRole === 'GOOGLE',
                                                'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200': account.socialRole === 'KAKAO',
                                                'bg-orange-100 text-orange-800 dark:bg-orange-900 dark:text-orange-200': account.socialRole === 'NAVER'
                                            }"
                                        >
                                            {{ account.socialRole }}
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.pi-eye {
    transform: scale(1.6);
    margin-right: 1rem;
}

.pi-eye-slash {
    transform: scale(1.6);
    margin-right: 1rem;
}
</style>
