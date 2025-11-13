<script setup lang="ts">
import { onMounted } from 'vue';

/**
 * 네이버 로그인 콜백 처리
 * 백엔드에서 리다이렉트된 데이터를 처리하고 부모 창에 결과를 전달
 */
onMounted(() => {
    const urlParams = new URLSearchParams(window.location.search);
    const success = urlParams.get('success');
    const error = urlParams.get('error');

    // 토큰은 HttpOnly 쿠키에 저장되므로 JavaScript로 읽을 수 없음
    // 백엔드의 JwtAuthenticationFilter가 쿠키에서 자동으로 토큰을 읽어서 인증 처리
    // 프론트엔드에서는 성공 여부만 확인하고, 토큰은 쿠키를 통해 자동으로 관리됨
    if (success === 'true') {
        handleSuccess();
    } else if (success === 'false' || error) {
        handleError(error || '로그인 처리 중 오류가 발생했습니다.');
    } else {
        // URL 파라미터가 없는 경우도 처리
        handleError('예상치 못한 오류가 발생했습니다.');
    }
});

/**
 * 로그인 성공 처리
 * 토큰은 HttpOnly 쿠키에 저장되어 있으므로 별도로 저장할 필요 없음
 */
function handleSuccess() {
    try {
        // 토큰은 쿠키에 저장되어 있으므로 localStorage에 저장하지 않음
        // API 요청 시 쿠키가 자동으로 전송되어 인증 처리됨
        // 로그인 상태 플래그만 저장
        localStorage.setItem('isLoggedIn', 'true');

        // 부모 창에 성공 메시지 전달
        sendMessageToParent({
            type: 'NAVER_LOGIN_SUCCESS'
        });
    } catch (e) {
        handleError('로그인 처리 중 오류가 발생했습니다.');
    }
}

/**
 * 로그인 오류 처리
 */
function handleError(errorMessage: string) {
    // 부모 창에 오류 메시지 전달
    sendMessageToParent({
        type: 'NAVER_LOGIN_ERROR',
        error: errorMessage
    });
}

function sendMessageToParent(message: any) {
    try {
        if (window.opener && !window.opener.closed) {
            window.opener.postMessage(message, window.location.origin);
        }
    } catch (error) {}

    // 메시지 전송 후 잠시 대기 후 창 닫기
    setTimeout(() => {
        try {
            window.close();
        } catch (error) {}
    }, 100);
}
</script>

<template>
    <div class="flex items-center justify-center min-h-screen bg-gray-50">
        <div class="text-center">
            <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-green-500 mx-auto mb-4"></div>
            <span class="text-lg text-gray-700">네이버 로그인 처리 중...</span>
        </div>
    </div>
</template>
