<script setup lang="ts">
import { onMounted } from 'vue';

/**
 * 네이버 로그인 콜백 처리
 * 백엔드에서 리다이렉트된 데이터를 처리하고 부모 창에 결과를 전달
 */
onMounted(() => {
    console.log('NaverCallback 페이지 로드됨');
    console.log('현재 URL:', window.location.href);
    console.log('URL 검색 파라미터:', window.location.search);

    const urlParams = new URLSearchParams(window.location.search);
    const success = urlParams.get('success');
    const accessToken = urlParams.get('accessToken');
    const error = urlParams.get('error');

    console.log('파싱된 URL 파라미터:', {
        success,
        accessToken: accessToken ? `${accessToken.substring(0, 20)}...` : null,
        error
    });

    if (success === 'true' && accessToken) {
        console.log('성공 케이스 처리 시작');
        handleSuccess(accessToken);
    } else if (success === 'false' || error) {
        console.log('오류 케이스 처리 시작');
        handleError(error || '로그인 처리 중 오류가 발생했습니다.');
    } else {
        // URL 파라미터가 없는 경우도 처리
        console.warn('URL 파라미터가 없습니다:', window.location.search);
        console.warn('모든 URL 파라미터:', Object.fromEntries(urlParams.entries()));
        handleError('예상치 못한 오류가 발생했습니다.');
    }
});

/**
 * 로그인 성공 처리
 */
function handleSuccess(accessToken: string) {
    try {
        console.log('네이버 로그인 성공:', accessToken);

        // 사용자 데이터 저장
        localStorage.setItem('accessToken', accessToken);

        // 부모 창에 성공 메시지 전달
        sendMessageToParent({
            type: 'NAVER_LOGIN_SUCCESS',
            accessToken: accessToken
        });
    } catch (e) {
        console.error('사용자 데이터 파싱 오류:', e);
        handleError('사용자 데이터 처리 중 오류가 발생했습니다.');
    }
}

/**
 * 로그인 오류 처리
 */
function handleError(errorMessage: string) {
    console.error('네이버 로그인 오류:', errorMessage);

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
</script>

<template>
    <div class="flex items-center justify-center min-h-screen bg-gray-50">
        <div class="text-center">
            <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-green-500 mx-auto mb-4"></div>
            <span class="text-lg text-gray-700">네이버 로그인 처리 중...</span>
        </div>
    </div>
</template>
