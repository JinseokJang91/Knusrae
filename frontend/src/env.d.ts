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

declare global {
    interface Window {
        naver: any;
    }
    const naver: any;
}
