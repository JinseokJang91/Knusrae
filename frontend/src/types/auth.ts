// 인증/회원 관련 타입 정의

/** 로그인한 회원 정보 (member-service /api/member/me 응답) */
export interface MemberInfo {
    id?: number;
    email?: string;
    name?: string;
    nickname?: string;
    profileImage?: string;
    bio?: string;
    followerCount?: number;
    followingCount?: number;
}

/** OAuth 제공자 */
export type OAuthProvider = 'naver' | 'kakao' | 'google';

/** OAuth 콜백 → 부모 창 postMessage 성공 메시지 (로그인 창에서 사용) */
export interface OAuthSuccessMessage {
    type: 'NAVER_LOGIN_SUCCESS' | 'KAKAO_LOGIN_SUCCESS' | 'GOOGLE_LOGIN_SUCCESS';
}

/** OAuth 콜백 → 부모 창 postMessage 실패 메시지 */
export interface OAuthErrorMessage {
    type: 'NAVER_LOGIN_ERROR' | 'KAKAO_LOGIN_ERROR' | 'GOOGLE_LOGIN_ERROR';
    error: string;
}

/** OAuth 콜백에서 부모 창으로 보내는 메시지 */
export type OAuthPostMessage = OAuthSuccessMessage | OAuthErrorMessage;
