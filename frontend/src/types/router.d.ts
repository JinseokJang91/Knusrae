import 'vue-router';

declare module 'vue-router' {
    interface RouteMeta {
        /** 로그인 필요 라우트 */
        requiresAuth?: boolean;
        /** 관리자 전용 라우트 */
        requiresAdmin?: boolean;
    }
}
