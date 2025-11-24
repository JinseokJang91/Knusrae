<script setup lang="ts">
import { onMounted } from 'vue';

interface Props {
    provider: 'naver' | 'kakao' | 'google';
}

const props = defineProps<Props>();

const providerConfig = {
    naver: {
        successType: 'NAVER_LOGIN_SUCCESS',
        errorType: 'NAVER_LOGIN_ERROR',
        color: 'green-500',
        label: '네이버'
    },
    kakao: {
        successType: 'KAKAO_LOGIN_SUCCESS',
        errorType: 'KAKAO_LOGIN_ERROR',
        color: 'yellow-400',
        label: '카카오'
    },
    google: {
        successType: 'GOOGLE_LOGIN_SUCCESS',
        errorType: 'GOOGLE_LOGIN_ERROR',
        color: 'blue-600',
        label: '구글'
    }
};

onMounted(() => {
    const urlParams = new URLSearchParams(window.location.search);
    const success = urlParams.get('success');
    const error = urlParams.get('error');

    if (success === 'true') {
        handleSuccess();
    } else if (success === 'false' || error) {
        handleError(error || '로그인 처리 중 오류가 발생했습니다.');
    } else {
        handleError('예상치 못한 오류가 발생했습니다.');
    }
});

function handleSuccess() {
    try {
        localStorage.setItem('isLoggedIn', 'true');

        sendMessageToParent({
            type: providerConfig[props.provider].successType
        });
    } catch (e) {
        console.error('로그인 처리 오류:', e);
        handleError('로그인 처리 중 오류가 발생했습니다.');
    }
}

function handleError(errorMessage: string) {
    sendMessageToParent({
        type: providerConfig[props.provider].errorType,
        error: errorMessage
    });
}

function sendMessageToParent(message: any) {
    try {
        if (window.opener && !window.opener.closed) {
            window.opener.postMessage(message, window.location.origin);
        }
    } catch (error) {
        console.error(error);
    }

    setTimeout(() => {
        try {
            window.close();
        } catch (error) {
            console.error(error);
        }
    }, 100);
}
</script>

<template>
    <div class="flex items-center justify-center min-h-screen bg-gray-50">
        <div class="text-center">
            <div 
                class="animate-spin rounded-full h-12 w-12 mx-auto mb-4"
                :class="`border-b-2 border-${providerConfig[provider].color}`"
            ></div>
            <p class="text-gray-600">{{ providerConfig[provider].label }} 로그인 처리 중...</p>
        </div>
    </div>
</template>

