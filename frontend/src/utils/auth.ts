import { httpJson } from '@/utils/http';
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

export async function fetchMemberInfo(): Promise<any> {
    try {
        // member-service의 BASE URL 사용 (환경 변수가 있으면 사용, 없으면 기본값)
        const API_BASE_URL = import.meta.env.VITE_API_BASE_URL_MEMBER;
        const memberInfo = await httpJson(API_BASE_URL, '/api/member/me', {
            method: 'GET'
        });

        console.log('사용자 정보:', memberInfo);
        return memberInfo;
    } catch (error) {
        console.error('사용자 정보 조회 실패:', error);
        // API 호출 실패 시 기본값 설정
        return null;
    }
}
