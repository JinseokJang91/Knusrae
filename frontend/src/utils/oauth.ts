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

export function openOAuthPopup(
    provider: 'naver' | 'google' | 'kakao',
    clientId: string,
    redirectUri: string,
    additionalParams: Record<string, string> = {}
): void {
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

    const handleMessage = (event: MessageEvent) => {
        if (event.origin !== window.location.origin) {
            return;
        }

        if (event.data.type === config.successType) {
            alert(`${provider} 로그인이 성공했습니다!`);
            window.location.href = '/';
        } else if (event.data.type === config.errorType) {
            alert(`${provider} 로그인 중 오류가 발생했습니다: ${event.data.error}`);
        }
    };

    window.addEventListener('message', handleMessage);

    const checkClosed = setInterval(() => {
        try {
            if (popup?.closed) {
                clearInterval(checkClosed);
                window.removeEventListener('message', handleMessage);
            }
        } catch (error) {
            console.error(error);
        }
    }, 1000);

    setTimeout(() => {
        clearInterval(checkClosed);
        window.removeEventListener('message', handleMessage);
    }, 300000);
}

