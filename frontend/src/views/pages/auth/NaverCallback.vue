<script setup lang="ts">
import { onMounted } from 'vue';

/**
 * 네이버 로그인 콜백 처리
 * 백엔드에서 리다이렉트된 데이터를 처리하고 부모 창에 결과를 전달
 */
onMounted(() => {
    const urlParams = new URLSearchParams(window.location.search);
    const success = urlParams.get('success');
    const user = urlParams.get('user');
    const error = urlParams.get('error');

    if (success === 'true' && user) {
        handleSuccess(user);
    } else if (success === 'false' || error) {
        handleError(error || '로그인 처리 중 오류가 발생했습니다.');
    } else {
        handleError('예상치 못한 오류가 발생했습니다.');
    }
});

/**
 * 로그인 성공 처리
 */
function handleSuccess(user: string) {
    try {
        const userData = JSON.parse(user);
        console.log('네이버 로그인 성공:', userData);

        // 사용자 데이터 저장
        localStorage.setItem('naver_user', user);

        // 부모 창에 성공 메시지 전달
        if (window.opener) {
            window.opener.postMessage(
                {
                    type: 'NAVER_LOGIN_SUCCESS',
                    user: userData
                },
                '*'
            );
        }

        // 팝업 닫기
        window.close();
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
    if (window.opener) {
        window.opener.postMessage(
            {
                type: 'NAVER_LOGIN_ERROR',
                error: errorMessage
            },
            '*'
        );
    }

    // 팝업 닫기
    window.close();
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
