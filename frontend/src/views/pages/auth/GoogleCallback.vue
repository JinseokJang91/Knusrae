<script setup lang="ts">
import { onMounted } from 'vue';

onMounted(() => {
    const urlParams = new URLSearchParams(window.location.search);
    const accessToken = urlParams.get('accessToken');
    const success = urlParams.get('success');
    const error = urlParams.get('error');

    if (success === 'true' && accessToken) {
        handleSuccess(accessToken);
    } else if (success === 'false' || error) {
        handleError(error || '로그인 처리 중 오류가 발생했습니다.');
    } else {
        // URL 파라미터가 없는 경우도 처리
        handleError('예상치 못한 오류가 발생했습니다.');
    }
});

/**
 * 로그인 성공 처리
 */
function handleSuccess(accessToken: string) {
    try {
        // 사용자 데이터 저장
        localStorage.setItem('accessToken', accessToken);

        // 부모 창에 성공 메시지 전달
        sendMessageToParent({
            type: 'GOOGLE_LOGIN_SUCCESS',
            accessToken: accessToken
        });
    } catch (e) {
        handleError('사용자 데이터 처리 중 오류가 발생했습니다.');
    }
}

/**
 * 로그인 오류 처리
 */
function handleError(errorMessage: string) {
    // 부모 창에 오류 메시지 전달
    sendMessageToParent({
        type: 'GOOGLE_LOGIN_ERROR',
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
            <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
            <p class="text-gray-600">구글 로그인 처리 중...</p>
        </div>
    </div>
</template>
