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
 */
export function getCurrentUser(): any {
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
 */
export function isLoggedIn(): boolean {
    return Boolean(localStorage.getItem('accessToken')) && isTokenValid();
}
