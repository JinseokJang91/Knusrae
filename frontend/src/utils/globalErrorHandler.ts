import type { App } from 'vue';
import type { Router } from 'vue-router';

const ERROR_PAGE_PATH = '/error/error';

let routerInstance: Router | null = null;
let isRedirecting = false;

function redirectToErrorPage() {
    if (isRedirecting) return;
    if (!routerInstance?.currentRoute.value) return;
    if (routerInstance.currentRoute.value.path === ERROR_PAGE_PATH) return;

    isRedirecting = true;
    routerInstance.push(ERROR_PAGE_PATH).finally(() => {
        isRedirecting = false;
    });
}

function logError(source: string, err: unknown, extra?: string) {
    const message = err instanceof Error ? err.message : String(err);
    const stack = err instanceof Error ? err.stack : undefined;
    console.error(`[${source}]`, message, extra ?? '', stack ?? '');
}

/**
 * Vue 앱/컴포넌트에서 발생한 처리되지 않은 에러
 */
function handleVueError(err: unknown, _instance: unknown, info: string) {
    logError('Vue', err, info);
    redirectToErrorPage();
}

/**
 * 라우터 에러: lazy 로드 실패, 네비게이션 가드 내 에러 등
 */
function handleRouterError(err: unknown) {
    logError('Router', err);
    redirectToErrorPage();
}

/**
 * 동기 스크립트에서 발생한 처리되지 않은 예외
 */
function handleWindowError(event: ErrorEvent) {
    logError('Window', event.error ?? event.message, event.filename);
    redirectToErrorPage();
    return true; // 기본 오류 처리도 수행
}

/**
 * 처리되지 않은 Promise rejection (예: API 예외를 catch하지 않은 경우)
 */
function handleUnhandledRejection(event: PromiseRejectionEvent) {
    logError('UnhandledRejection', event.reason);
    redirectToErrorPage();
}

/**
 * 전역 에러 핸들러를 설치합니다.
 * - Vue 컴포넌트/렌더 에러
 * - 라우터 lazy 로드 실패·네비게이션 에러
 * - window.onerror / unhandledrejection
 */
export function installGlobalErrorHandler(app: App, router: Router): void {
    routerInstance = router;

    app.config.errorHandler = handleVueError;

    router.onError(handleRouterError);

    window.addEventListener('error', handleWindowError);
    window.addEventListener('unhandledrejection', handleUnhandledRejection);
}
