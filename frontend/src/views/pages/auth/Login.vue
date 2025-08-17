<script setup lang="ts">
import loginIconNaver from '@/assets/images/login-icon-naver.png';
import logoImage from '@/assets/images/logo-image.png';
import FloatingConfigurator from '@/components/FloatingConfigurator.vue';
import { ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const email = ref('');
const password = ref('');
const checked = ref(false);

const NAVER_CLIENT_ID = import.meta.env.VITE_NAVER_CLIENT_ID;
const NAVER_REDIRECT_URI = import.meta.env.VITE_NAVER_REDIRECT_URI;

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
    const state = Math.random().toString(36).substr(2, 11);
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
        if (event.data.type === 'NAVER_LOGIN_SUCCESS') {
            console.log('네이버 로그인 성공:', event.data.user);
            localStorage.setItem('naver_user', JSON.stringify(event.data.user));
            alert('네이버 로그인이 성공했습니다!');
            goHome();
        } else if (event.data.type === 'NAVER_LOGIN_ERROR') {
            console.error('네이버 로그인 오류:', event.data.error);
            alert('네이버 로그인 중 오류가 발생했습니다: ' + event.data.error);
        }
    };

    window.addEventListener('message', handleMessage);

    // 팝업 닫힘 감지
    const checkClosed = setInterval(() => {
        if (popup?.closed) {
            clearInterval(checkClosed);
            window.removeEventListener('message', handleMessage);
        }
    }, 1000);
}

/**
 * 홈으로 이동
 */
function goHome() {
    router.push('/');
}
</script>

<template>
    <FloatingConfigurator />
    <div class="bg-surface-50 dark:bg-surface-950 flex items-center justify-center min-h-screen min-w-[100vw] overflow-hidden relative">
        <!-- 홈 아이콘 -->
        <button @click="goHome" class="absolute top-6 left-6 p-3 bg-surface-100 dark:bg-surface-800 rounded-full hover:bg-surface-200 dark:hover:bg-surface-700 transition-colors z-10" title="홈으로 돌아가기">
            <svg class="w-6 h-6 text-surface-700 dark:text-surface-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"></path>
            </svg>
        </button>

        <div class="flex flex-col items-center justify-center">
            <div style="border-radius: 56px; padding: 0.3rem; background: linear-gradient(180deg, var(--primary-color) 10%, rgba(33, 150, 243, 0) 30%)">
                <div class="w-full bg-surface-0 dark:bg-surface-900 py-20 px-8 sm:px-20" style="border-radius: 53px">
                    <div class="text-center mb-8">
                        <img :src="logoImage" alt="로고 이미지" class="logo-image w-16 h-auto mx-auto mb-8" />
                        <div class="text-surface-900 dark:text-surface-0 text-3xl font-medium mb-4">로그인 / 회원가입</div>
                    </div>

                    <div>
                        <label for="loginId" class="block text-surface-900 dark:text-surface-0 text-xl font-medium mb-2">아이디</label>
                        <InputText id="loginId" type="text" placeholder="Login ID" class="w-full md:w-[30rem] mb-8" v-model="email" />

                        <label for="loginPassword" class="block text-surface-900 dark:text-surface-0 font-medium text-xl mb-2">패스워드</label>
                        <Password id="loginPassword" v-model="password" placeholder="Login Password" :toggleMask="true" class="mb-4" fluid :feedback="false" />

                        <div class="flex items-center justify-between mt-2 mb-8 gap-8">
                            <div class="flex items-center">
                                <Checkbox v-model="checked" id="rememberme1" binary class="mr-2"></Checkbox>
                                <label for="rememberme1">로그인 유지</label>
                            </div>
                            <span class="font-medium no-underline ml-2 text-right cursor-pointer text-primary">패스워드 찾기</span>
                        </div>

                        <Button label="Sign In" class="w-full" as="router-link" to="/">로그인</Button>

                        <div class="mt-4">
                            <button @click="handleNaverLogin" class="flex items-center justify-center w-full p-3 bg-green-500 hover:bg-green-600 text-white rounded-lg transition-colors">
                                <img :src="loginIconNaver" alt="네이버 로고" class="w-6 h-6 mr-2" />
                                <span>네이버로 로그인</span>
                            </button>
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
