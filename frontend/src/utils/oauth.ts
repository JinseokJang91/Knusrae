import { useAuthStore } from '@/stores/authStore';
import router from '@/router';

interface OAuthConfig {
    clientId: string;
    redirectUri: string;
    authUrl: string;
    successType: string;
    errorType: string;
    windowName: string;
}

const oauthConfigs: Record<string, Omit<OAuthConfig, 'clientId' | 'redirectUri'>> = {
    naver: {
        authUrl: 'https://nid.naver.com/oauth2.0/authorize',
        successType: 'NAVER_LOGIN_SUCCESS',
        errorType: 'NAVER_LOGIN_ERROR',
        windowName: 'naverLogin'
    },
    google: {
        authUrl: 'https://accounts.google.com/o/oauth2/v2/auth',
        successType: 'GOOGLE_LOGIN_SUCCESS',
        errorType: 'GOOGLE_LOGIN_ERROR',
        windowName: 'googleLogin'
    },
    kakao: {
        authUrl: 'https://kauth.kakao.com/oauth/authorize',
        successType: 'KAKAO_LOGIN_SUCCESS',
        errorType: 'KAKAO_LOGIN_ERROR',
        windowName: 'kakaoLogin'
    }
};

export function openOAuthPopup(provider: 'naver' | 'google' | 'kakao', clientId: string, redirectUri: string, additionalParams: Record<string, string> = {}): void {
    if (!clientId || !redirectUri) {
        alert(`${provider} 로그인 설정이 완료되지 않았습니다. 환경 변수를 확인해주세요.`);
        return;
    }

    const config = oauthConfigs[provider];
    const state = Math.random().toString(36).slice(2, 13);
    localStorage.setItem(`${provider}_state`, state);

    const params = new URLSearchParams({
        response_type: 'code',
        client_id: clientId,
        redirect_uri: redirectUri,
        state,
        ...additionalParams
    });

    const authUrl = `${config.authUrl}?${params.toString()}`;
    const popup = window.open(authUrl, config.windowName, 'width=500,height=600,scrollbars=yes,resizable=yes');

    if (!popup) {
        alert('팝업이 차단되었습니다. 팝업 차단을 해제해주세요.');
        return;
    }

    let messageReceived = false;

    // 메시지 리스너를 먼저 등록 (타이밍 이슈 방지)
    const handleMessage = async (event: MessageEvent) => {
        // origin 체크 - 보안을 위해 필수
        const isSameOrigin = event.origin === window.location.origin;
        const isLocalhost = event.origin.includes('localhost') && window.location.origin.includes('localhost');

        if (!isSameOrigin && !isLocalhost) {
            return;
        }

        // 메시지 타입 확인
        if (!event.data || typeof event.data !== 'object' || !event.data.type) {
            return;
        }

        // 메시지를 받았다고 표시
        messageReceived = true;

        if (event.data.type === config.successType) {
            // 리스너 제거 (중복 처리 방지)
            window.removeEventListener('message', handleMessage);
            clearInterval(checkClosed);

            // 팝업을 즉시 닫기
            try {
                if (popup && !popup.closed) {
                    popup.close();
                }
            } catch {
                // 팝업 닫기 실패는 무시
            }

            // authStore 업데이트
            const authStore = useAuthStore();
            await authStore.login();

            // localStorage에서 redirect 경로 가져오기
            const redirectPath = localStorage.getItem('oauth_redirect') || '/';
            localStorage.removeItem('oauth_redirect');

            // 현재 경로 확인
            const currentPath = router.currentRoute.value.path;

            // 로그인 페이지에서 메인으로 이동하는 경우 강제 새로고침
            if (currentPath === '/auth/login' || currentPath.startsWith('/auth/')) {
                window.location.href = redirectPath;
                return;
            }

            // router를 사용하여 이동 시도
            router
                .replace(redirectPath)
                .then(() => {
                    // 이동 후 경로 확인
                    setTimeout(() => {
                        if (router.currentRoute.value.path !== redirectPath) {
                            window.location.href = redirectPath;
                        }
                    }, 100);
                })
                .catch(() => {
                    // router.replace 실패 시 강제로 이동
                    window.location.href = redirectPath;
                });
        } else if (event.data.type === config.errorType) {
            // 리스너 제거
            window.removeEventListener('message', handleMessage);
            clearInterval(checkClosed);

            // 팝업을 즉시 닫기
            try {
                if (popup && !popup.closed) {
                    popup.close();
                }
            } catch {
                // 팝업 닫기 실패는 무시
            }

            alert(`${provider} 로그인 중 오류가 발생했습니다: ${event.data.error}`);
            localStorage.removeItem('oauth_redirect');
        }
    };

    // 메시지 리스너 등록
    window.addEventListener('message', handleMessage);

    // localStorage를 통한 대체 방법 처리 (postMessage 실패 시 사용)
    const checkLocalStorage = setInterval(() => {
        try {
            const eventKey = localStorage.getItem('oauth_callback_event');
            if (eventKey) {
                const eventData = localStorage.getItem(eventKey);
                if (eventData) {
                    const message = JSON.parse(eventData);

                    // 메시지 처리
                    if (message.type === config.successType || message.type === config.errorType) {
                        messageReceived = true;
                        clearInterval(checkLocalStorage);
                        localStorage.removeItem('oauth_callback_event');
                        localStorage.removeItem(eventKey);

                        // handleMessage와 동일한 로직 실행
                        handleMessage({
                            origin: window.location.origin,
                            data: message
                        } as MessageEvent);
                    }
                }
            }
        } catch (error) {
            console.error('[OAuth] localStorage 체크 중 오류:', error);
        }
    }, 500);

    // 팝업이 닫혔는지 확인하는 인터벌
    const checkClosed = setInterval(() => {
        try {
            if (popup?.closed) {
                // 메시지를 받지 못했다면 잠시 대기 후 리스너 제거
                if (!messageReceived) {
                    setTimeout(() => {
                        if (!messageReceived) {
                            clearInterval(checkClosed);
                            clearInterval(checkLocalStorage);
                            window.removeEventListener('message', handleMessage);
                        }
                    }, 3000);
                } else {
                    clearInterval(checkClosed);
                    clearInterval(checkLocalStorage);
                }
            }
        } catch (error) {
            console.error('[OAuth] 팝업 상태 확인 중 오류:', error);
        }
    }, 500);

    // 5분 후 타임아웃
    setTimeout(() => {
        clearInterval(checkClosed);
        clearInterval(checkLocalStorage);
        window.removeEventListener('message', handleMessage);
    }, 300000);
}
