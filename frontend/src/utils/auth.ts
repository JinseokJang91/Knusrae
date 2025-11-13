/**
 * JWT 토큰 디코딩 유틸리티
 */

/**
 * JWT 토큰을 디코딩하여 페이로드를 반환
 */
export function decodeJWT(token: string): any {
    try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(
            atob(base64)
                .split('')
                .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
                .join('')
        );
        return JSON.parse(jsonPayload);
    } catch (error) {
        console.error('JWT 디코딩 오류:', error);
        return null;
    }
}

/**
 * 로컬 스토리지에서 액세스 토큰을 가져와서 사용자 정보를 반환
 * HttpOnly 쿠키는 JavaScript로 읽을 수 없으므로 localStorage만 확인
 */
export function getCurrentUser(): any {
    // HttpOnly 쿠키는 JavaScript로 읽을 수 없으므로 localStorage만 확인
    const token = localStorage.getItem('accessToken');
    
    if (!token) {
        return null;
    }

    const decoded = decodeJWT(token);
    if (!decoded) {
        return null;
    }

    return {
        id: decoded.sub || decoded.userId,
        name: decoded.name || decoded.username,
        email: decoded.email,
        exp: decoded.exp
    };
}

/**
 * 토큰이 유효한지 확인 (만료 시간 체크)
 */
export function isTokenValid(): boolean {
    const user = getCurrentUser();
    if (!user || !user.exp) {
        return false;
    }

    const currentTime = Math.floor(Date.now() / 1000);
    return user.exp > currentTime;
}

/**
 * 로그인 상태 확인
 * HttpOnly 쿠키는 JavaScript로 읽을 수 없으므로 localStorage의 플래그를 확인
 * 실제 인증은 백엔드의 JwtAuthenticationFilter가 쿠키에서 처리
 * 
 * 참고: 로그인 성공 시 localStorage에 'isLoggedIn' 플래그가 저장됨
 */
export function isLoggedIn(): boolean {
    // HttpOnly 쿠키는 JavaScript로 읽을 수 없으므로 localStorage의 플래그 확인
    // 실제 인증은 백엔드에서 쿠키를 통해 처리됨
    const isLoggedInFlag = localStorage.getItem('isLoggedIn');
    return isLoggedInFlag === 'true';
}
