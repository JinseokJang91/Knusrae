/// <reference types="vite/client" />

declare module '*.vue' {
    import type { DefineComponent } from 'vue';
    const component: DefineComponent<{}, {}, any>;
    export default component;
}

interface ImportMetaEnv {
    readonly VITE_APP_TITLE: string;
    readonly VITE_NAVER_CLIENT_ID: string;
    readonly VITE_NAVER_REDIRECT_URI: string;
    readonly VITE_GOOGLE_CLIENT_ID: string;
    readonly VITE_GOOGLE_REDIRECT_URI: string;
    readonly VITE_KAKAO_CLIENT_ID: string;
    readonly VITE_KAKAO_REDIRECT_URI: string;
}

interface ImportMeta {
    readonly env: ImportMetaEnv;
}

/** 네이버 로그인 SDK (스크립트 로드 시 전역 노출) — 최소 인터페이스 */
interface NaverLoginSdk {
    Login?: {
        init?(options: Record<string, unknown>): void;
        getLoginStatus?(callback: (status: unknown) => void): void;
        logout?(): void;
    };
}

declare global {
    interface Window {
        naver?: NaverLoginSdk;
    }
    const naver: NaverLoginSdk | undefined;
}
