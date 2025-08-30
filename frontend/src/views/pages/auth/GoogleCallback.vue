<script setup lang="ts">
import { onMounted } from 'vue';

onMounted(() => {
    const urlParams = new URLSearchParams(window.location.search);
    const code = urlParams.get('code');
    const state = urlParams.get('state');
    const error = urlParams.get('error');

    // 에러가 있는 경우
    if (error) {
        sendMessageToParent({
            type: 'GOOGLE_LOGIN_ERROR',
            error: error
        });
        return;
    }

    // 코드가 없는 경우
    if (!code) {
        sendMessageToParent({
            type: 'GOOGLE_LOGIN_ERROR',
            error: '인증 코드를 받지 못했습니다.'
        });
        return;
    }

    // State 검증
    const savedState = localStorage.getItem('google_state');
    if (state !== savedState) {
        sendMessageToParent({
            type: 'GOOGLE_LOGIN_ERROR',
            error: '보안 검증에 실패했습니다.'
        });
        return;
    }

    // 백엔드로 인증 코드 전송
    exchangeCodeForToken(code);
});

function sendMessageToParent(message: any) {
    try {
        if (window.opener && !window.opener.closed) {
            window.opener.postMessage(message, window.location.origin);
        }
    } catch (error) {
        console.error('부모 창에 메시지 전송 실패:', error);
    }

    // 메시지 전송 후 잠시 대기 후 창 닫기
    setTimeout(() => {
        try {
            window.close();
        } catch (error) {
            console.error('창 닫기 실패:', error);
        }
    }, 100);
}

async function exchangeCodeForToken(code: string) {
    try {
        const response = await fetch('/api/auth/google/callback', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                code: code,
                redirect_uri: import.meta.env.VITE_GOOGLE_REDIRECT_URI
            })
        });

        if (!response.ok) {
            throw new Error('토큰 교환에 실패했습니다.');
        }

        const data = await response.json();

        // 부모 창에 성공 메시지 전송
        sendMessageToParent({
            type: 'GOOGLE_LOGIN_SUCCESS',
            accessToken: data.accessToken,
            user: data.user
        });
    } catch (error) {
        console.error('토큰 교환 오류:', error);
        sendMessageToParent({
            type: 'GOOGLE_LOGIN_ERROR',
            error: error instanceof Error ? error.message : '알 수 없는 오류가 발생했습니다.'
        });
    }
}
</script>

<template>
    <div class="flex items-center justify-center min-h-screen bg-gray-50">
        <div class="text-center">
            <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
            <p class="text-gray-600">구글 로그인 처리 중...</p>
        </div>
    </div>
</template>
