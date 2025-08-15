<script setup lang="ts">
import { onMounted } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();

onMounted(async () => {
    const urlParams = new URLSearchParams(window.location.search);
    const code = urlParams.get('code');
    const state = urlParams.get('state');
    const error = urlParams.get('error');

    // 에러 체크
    if (error) {
        console.error('네이버 로그인 에러:', error);
        router.replace('/auth/login');
        return;
    }

    // state 검증 (CSRF 방지)
    const savedState = localStorage.getItem('naver_state');
    if (state !== savedState) {
        console.error('State 불일치');
        router.replace('/auth/login');
        return;
    }

    if (code) {
        try {
            // 액세스 토큰 요청
            const tokenResponse = await fetch('https://nid.naver.com/oauth2.0/token', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: new URLSearchParams({
                    grant_type: 'authorization_code',
                    client_id: import.meta.env.VITE_NAVER_CLIENT_ID,
                    client_secret: import.meta.env.VITE_NAVER_CLIENT_SECRET || '',
                    code: code,
                    state: state || ''
                })
            });

            const tokenData = await tokenResponse.json();

            if (tokenData.access_token) {
                // 사용자 정보 요청
                const userResponse = await fetch('https://openapi.naver.com/v1/nid/me', {
                    headers: {
                        Authorization: `Bearer ${tokenData.access_token}`
                    }
                });

                const userData = await userResponse.json();

                // 데이터 저장
                localStorage.setItem('naver_access_token', tokenData.access_token);
                localStorage.setItem('naver_user', JSON.stringify(userData.response));
                localStorage.removeItem('naver_state');

                // 팝업 닫기
                window.close();
            } else {
                throw new Error('토큰 요청 실패');
            }
        } catch (error) {
            console.error('네이버 API 호출 에러:', error);
            router.replace('/auth/login');
        }
    } else {
        router.replace('/auth/login');
    }
});
</script>

<template>
    <div class="flex items-center justify-center min-h-screen">
        <span>네이버 로그인 처리 중...</span>
    </div>
</template>
