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
        color: 'orange-600',
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
        // Token은 HttpOnly Cookie로 저장되므로 별도 처리 불필요
        // 부모 창에 메시지 전송하여 로그인 상태 업데이트
        sendMessageToParent({
            type: providerConfig[props.provider].successType
        });
    } catch (e) {
        console.error('[OAuthCallback] 로그인 처리 오류:', e);
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
    const sendMessage = () => {
        try {
            if (!window.opener) {
                return false;
            }
            
            try {
                if (window.opener.closed) {
                    return false;
                }
            } catch (e) {
                // 크로스 오리진일 수 있음, 계속 진행
            }
            
            window.opener.postMessage(message, window.location.origin);
            return true;
        } catch (error) {
            return false;
        }
    };

    // localStorage를 통한 대체 방법 (postMessage 실패 시 사용)
    try {
        const eventKey = `oauth_${props.provider}_callback_${Date.now()}`;
        localStorage.setItem(eventKey, JSON.stringify(message));
        localStorage.setItem('oauth_callback_event', eventKey);
    } catch (e) {
        console.error('[OAuthCallback] localStorage 저장 실패:', e);
    }

    // postMessage 시도 및 재시도
    sendMessage();
    setTimeout(() => sendMessage(), 100);
    setTimeout(() => sendMessage(), 200);
    setTimeout(() => sendMessage(), 300);
    setTimeout(() => sendMessage(), 500);
    setTimeout(() => sendMessage(), 1000);

    // 팝업 닫기
    setTimeout(() => {
        try {
            window.close();
        } catch (error) {
            // 창 닫기 실패는 무시
        }
    }, 1500);
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

