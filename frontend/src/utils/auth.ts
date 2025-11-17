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
