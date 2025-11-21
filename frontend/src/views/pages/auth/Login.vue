<script setup lang="ts">
import loginIconNaver from '@/assets/images/login-icon-naver.png';
import logoImage from '@/assets/images/logo-text.png';

import { useRouter } from 'vue-router';
import { ref } from 'vue';

const router = useRouter();

// 개발 환경 체크
const isDevelopment = import.meta.env.DEV;

// 테스트 계정 관련
const showTestAccounts = ref(false);
const testAccounts = ref<any[]>([]);
const loadingTestAccounts = ref(false);

// 환경 변수
const NAVER_CLIENT_ID = import.meta.env.VITE_NAVER_CLIENT_ID;
const NAVER_REDIRECT_URI = import.meta.env.VITE_NAVER_REDIRECT_URI;
const GOOGLE_CLIENT_ID = import.meta.env.VITE_GOOGLE_CLIENT_ID;
const GOOGLE_REDIRECT_URI = import.meta.env.VITE_GOOGLE_REDIRECT_URI;
const KAKAO_CLIENT_ID = import.meta.env.VITE_KAKAO_CLIENT_ID;
const KAKAO_REDIRECT_URI = import.meta.env.VITE_KAKAO_REDIRECT_URI;

/**
 * 네이버 로그인 처리
 */
function handleNaverLogin() {
    // 환경 변수 체크
    if (!NAVER_CLIENT_ID || !NAVER_REDIRECT_URI) {
        alert('네이버 로그인 설정이 완료되지 않았습니다. 환경 변수를 확인해주세요.');
        return;
    }

    // CSRF 방지를 위한 state 생성
    const state = Math.random().toString(36).slice(2, 13);
    localStorage.setItem('naver_state', state);

    // 네이버 OAuth URL 생성
    const naverAuthUrl = `https://nid.naver.com/oauth2.0/authorize?response_type=code&client_id=${NAVER_CLIENT_ID}&redirect_uri=${encodeURIComponent(NAVER_REDIRECT_URI)}&state=${state}`;

    // 팝업으로 열기
    const popup = window.open(naverAuthUrl, 'naverLogin', 'width=500,height=600,scrollbars=yes,resizable=yes');

    if (!popup) {
        alert('팝업이 차단되었습니다. 팝업 차단을 해제해주세요.');
        return;
    }

    // 팝업에서 메시지 수신
    const handleMessage = (event: MessageEvent) => {
        // 보안을 위해 origin 검증
        if (event.origin !== window.location.origin) {
            return;
        }

        if (event.data.type === 'NAVER_LOGIN_SUCCESS') {
            // 토큰은 HttpOnly 쿠키에 저장되어 있으므로 localStorage에 저장하지 않음
            alert('네이버 로그인이 성공했습니다!');
            goHome();
        } else if (event.data.type === 'NAVER_LOGIN_ERROR') {
            alert('네이버 로그인 중 오류가 발생했습니다: ' + event.data.error);
        }
    };

    window.addEventListener('message', handleMessage);

    // 팝업 닫힘 감지 (더 안전한 방식)
    const checkClosed = setInterval(() => {
        try {
            if (popup?.closed) {
                clearInterval(checkClosed);
                window.removeEventListener('message', handleMessage);
            }
        } catch (error) {
            // CORS 오류가 발생할 수 있으므로 무시
        }
    }, 1000);

    // 5분 후 자동으로 정리
    setTimeout(() => {
        clearInterval(checkClosed);
        window.removeEventListener('message', handleMessage);
    }, 300000);
}

/**
 * 구글 로그인 처리
 */
function handleGoogleLogin() {
    // 환경 변수 체크
    if (!GOOGLE_CLIENT_ID || !GOOGLE_REDIRECT_URI) {
        alert('구글 로그인 설정이 완료되지 않았습니다. 환경 변수를 확인해주세요.');
        return;
    }

    // CSRF 방지를 위한 state 생성
    const state = Math.random().toString(36).slice(2, 13);
    localStorage.setItem('google_state', state);

    // 구글 OAuth URL 생성
    const googleAuthUrl = `https://accounts.google.com/o/oauth2/v2/auth?response_type=code&client_id=${GOOGLE_CLIENT_ID}&redirect_uri=${encodeURIComponent(GOOGLE_REDIRECT_URI)}&scope=openid%20email%20profile&state=${state}`;

    // 팝업으로 열기
    const popup = window.open(googleAuthUrl, 'googleLogin', 'width=500,height=600,scrollbars=yes,resizable=yes');

    if (!popup) {
        alert('팝업이 차단되었습니다. 팝업 차단을 해제해주세요.');
        return;
    }

    // 팝업에서 메시지 수신
    const handleMessage = (event: MessageEvent) => {
        // 보안을 위해 origin 검증
        if (event.origin !== window.location.origin) {
            return;
        }

        if (event.data.type === 'GOOGLE_LOGIN_SUCCESS') {
            // 토큰은 HttpOnly 쿠키에 저장되어 있으므로 localStorage에 저장하지 않음
            alert('구글 로그인이 성공했습니다!');
            goHome();
        } else if (event.data.type === 'GOOGLE_LOGIN_ERROR') {
            alert('구글 로그인 중 오류가 발생했습니다: ' + event.data.error);
        }
    };

    window.addEventListener('message', handleMessage);

    // 팝업 닫힘 감지 (더 안전한 방식)
    const checkClosed = setInterval(() => {
        try {
            if (popup?.closed) {
                clearInterval(checkClosed);
                window.removeEventListener('message', handleMessage);
            }
        } catch (error) {
            // CORS 오류가 발생할 수 있으므로 무시
        }
    }, 1000);

    // 5분 후 자동으로 정리
    setTimeout(() => {
        clearInterval(checkClosed);
        window.removeEventListener('message', handleMessage);
    }, 300000);
}

/**
 * 카카오 로그인 처리
 */
function handleKakaoLogin() {
    // 환경 변수 체크
    if (!KAKAO_CLIENT_ID || !KAKAO_REDIRECT_URI) {
        alert('카카오 로그인 설정이 완료되지 않았습니다. 환경 변수를 확인해주세요.');
        return;
    }

    // CSRF 방지를 위한 state 생성
    const state = Math.random().toString(36).slice(2, 13);
    localStorage.setItem('kakao_state', state);

    // 카카오 OAuth URL 생성
    const kakaoAuthUrl = `https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=${KAKAO_CLIENT_ID}&redirect_uri=${encodeURIComponent(KAKAO_REDIRECT_URI)}&state=${state}`;

    // 팝업으로 열기
    const popup = window.open(kakaoAuthUrl, 'kakaoLogin', 'width=500,height=600,scrollbars=yes,resizable=yes');

    if (!popup) {
        alert('팝업이 차단되었습니다. 팝업 차단을 해제해주세요.');
        return;
    }

    // 팝업에서 메시지 수신
    const handleMessage = (event: MessageEvent) => {
        // 보안을 위해 origin 검증
        if (event.origin !== window.location.origin) {
            return;
        }

        if (event.data.type === 'KAKAO_LOGIN_SUCCESS') {
            // 토큰은 HttpOnly 쿠키에 저장되어 있으므로 localStorage에 저장하지 않음
            alert('카카오 로그인이 성공했습니다!');
            goHome();
        } else if (event.data.type === 'KAKAO_LOGIN_ERROR') {
            alert('카카오 로그인 중 오류가 발생했습니다: ' + event.data.error);
        }
    };

    window.addEventListener('message', handleMessage);

    // 팝업 닫힘 감지 (더 안전한 방식)
    const checkClosed = setInterval(() => {
        try {
            if (popup?.closed) {
                clearInterval(checkClosed);
                window.removeEventListener('message', handleMessage);
            }
        } catch (error) {
            // CORS 오류가 발생할 수 있으므로 무시
        }
    }, 1000);

    // 5분 후 자동으로 정리
    setTimeout(() => {
        clearInterval(checkClosed);
        window.removeEventListener('message', handleMessage);
    }, 300000);
}

/**
 * 홈으로 이동
 */
function goHome() {
    router.push('/');
}

/**
 * 테스트 계정 목록 가져오기
 */
async function loadTestAccounts() {
    if (!isDevelopment) return;
    
    loadingTestAccounts.value = true;
    try {
        const response = await fetch('http://localhost:8081/api/auth/test/accounts', {
            method: 'GET',
            credentials: 'include',
        });
        
        if (response.ok) {
            testAccounts.value = await response.json();
            showTestAccounts.value = true;
        } else {
            alert('테스트 계정 목록을 가져오는데 실패했습니다.');
        }
    } catch (error) {
        console.error('테스트 계정 로드 에러:', error);
        alert('테스트 계정 목록을 가져오는 중 오류가 발생했습니다.');
    } finally {
        loadingTestAccounts.value = false;
    }
}

/**
 * 테스트 계정으로 로그인
 */
async function loginWithTestAccount(email: string) {
    try {
        const response = await fetch('http://localhost:8081/api/auth/test/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            credentials: 'include',
            body: JSON.stringify({ email }),
        });
        
        if (response.ok) {
            alert(`${email} 계정으로 로그인되었습니다!`);
            localStorage.setItem('isLoggedIn', 'true');
            
            goHome();
        } else {
            const error = await response.json();
            alert(`로그인 실패: ${error.message || '알 수 없는 오류'}`);
        }
    } catch (error) {
        console.error('테스트 로그인 에러:', error);
        alert('로그인 중 오류가 발생했습니다.');
    }
}
</script>

<template>
    <div class="bg-gray-50 dark:bg-gray-950 flex items-center justify-center min-h-screen min-w-[100vw] overflow-hidden relative">
        <!-- 홈 아이콘 -->
        <button @click="goHome" class="absolute top-6 left-6 p-3 bg-gray-100 dark:bg-gray-800 rounded-full hover:bg-gray-200 dark:hover:bg-gray-700 transition-colors z-10" title="홈으로 돌아가기">
            <svg class="w-6 h-6 text-gray-700 dark:text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"></path>
            </svg>
        </button>

        <div class="flex flex-col items-center justify-center">
            <div style="border-radius: 56px; padding: 0.3rem; background: linear-gradient(180deg, var(--primary-color) 10%, rgba(33, 150, 243, 0) 50%)">
                <div class="w-full bg-white dark:bg-gray-900 py-20 px-8 sm:px-20" style="border-radius: 53px">
                    <div class="text-center mb-8">
                        <img :src="logoImage" alt="로고 이미지" class="logo-image w-50 h-auto mx-auto mb-8" />
                    </div>

                    <div class="space-y-4">
                        <!-- 구글 로그인 버튼 -->
                        <button @click="handleGoogleLogin" class="w-full flex items-center justify-center gap-3 bg-white border border-gray-300 rounded-lg px-6 py-3 text-gray-700 hover:bg-gray-50 transition-colors">
                            <svg class="w-5 h-5" viewBox="0 0 24 24">
                                <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" />
                                <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" />
                                <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" />
                                <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" />
                            </svg>
                            <span class="font-medium">Google로 로그인</span>
                        </button>

                        <!-- 카카오 로그인 버튼 -->
                        <button @click="handleKakaoLogin" class="w-full flex items-center justify-center gap-3 bg-yellow-400 rounded-lg px-6 py-3 text-gray-900 hover:bg-yellow-500 transition-colors">
                            <svg class="w-5 h-5" viewBox="0 0 24 24" fill="currentColor">
                                <path d="M12 3c5.799 0 10.5 3.664 10.5 8.185 0 4.52-4.701 8.184-10.5 8.184a13.5 13.5 0 0 1-1.727-.11l-4.408 2.883c-.501.265-.678.236-.472-.413l.892-3.678c-2.88-1.46-4.785-3.99-4.785-6.866C1.5 6.665 6.201 3 12 3z" />
                            </svg>
                            <span class="font-medium">카카오로 로그인</span>
                        </button>

                        <!-- 네이버 로그인 버튼 -->
                        <button @click="handleNaverLogin" class="w-full flex items-center justify-center gap-3 bg-green-500 rounded-lg px-6 py-3 text-white hover:bg-green-600 transition-colors">
                            <img :src="loginIconNaver" alt="네이버 로고" class="w-5 h-5" />
                            <span class="font-medium">네이버로 로그인</span>
                        </button>

                        <!-- 개발 환경에서만 표시되는 테스트 계정 로그인 -->
                        <div v-if="isDevelopment" class="mt-8 pt-6 border-t border-gray-300 dark:border-gray-700">
                            <button @click="loadTestAccounts" :disabled="loadingTestAccounts" class="w-full flex items-center justify-center gap-2 bg-purple-100 dark:bg-purple-900 border border-purple-300 dark:border-purple-700 rounded-lg px-6 py-3 text-purple-700 dark:text-purple-300 hover:bg-purple-200 dark:hover:bg-purple-800 transition-colors disabled:opacity-50">
                                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
                                </svg>
                                <span class="font-medium">{{ loadingTestAccounts ? '로딩 중...' : '테스트 계정으로 로그인' }}</span>
                            </button>

                            <!-- 테스트 계정 목록 -->
                            <div v-if="showTestAccounts" class="mt-4 space-y-2">
                                <div v-for="account in testAccounts" :key="account.id" @click="loginWithTestAccount(account.email)" class="p-3 bg-gray-100 dark:bg-gray-800 rounded-lg hover:bg-gray-200 dark:hover:bg-gray-700 transition-colors cursor-pointer">
                                    <div class="flex items-center justify-between">
                                        <div class="flex-1">
                                            <div class="font-medium text-gray-900 dark:text-gray-100">{{ account.name }}</div>
                                            <div class="text-sm text-gray-600 dark:text-gray-400">{{ account.email }}</div>
                                        </div>
                                        <div class="text-xs px-2 py-1 rounded" :class="{
                                            'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200': account.socialRole === 'GOOGLE',
                                            'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200': account.socialRole === 'KAKAO',
                                            'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200': account.socialRole === 'NAVER'
                                        }">
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
