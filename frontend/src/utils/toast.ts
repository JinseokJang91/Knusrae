import { useToast } from 'primevue/usetoast';

export type ToastSeverity = 'success' | 'info' | 'warn' | 'error';

/**
 * PrimeVue Toast는 message.life가 있을 때만 자동으로 닫힌다 (ToastMessage.vue: if (this.message.life)).
 * life 미설정 시 토스트가 화면에 계속 남는다.
 */
const DEFAULT_TOAST_LIFE_MS: Record<ToastSeverity, number> = {
    success: 3000,
    info: 3000,
    warn: 4000,
    error: 5000
};

/**
 * Toast 메시지를 표시하는 헬퍼 함수들
 */
export const useAppToast = () => {
    const toast = useToast();

    /**
     * 성공 메시지 표시
     */
    const showSuccess = (message: string, summary?: string) => {
        toast.add({
            severity: 'success',
            summary: summary || '성공',
            detail: message,
            life: DEFAULT_TOAST_LIFE_MS.success
        });
    };

    /**
     * 정보 메시지 표시
     */
    const showInfo = (message: string, summary?: string) => {
        toast.add({
            severity: 'info',
            summary: summary || '알림',
            detail: message,
            life: DEFAULT_TOAST_LIFE_MS.info
        });
    };

    /**
     * 경고 메시지 표시
     */
    const showWarn = (message: string, summary?: string) => {
        toast.add({
            severity: 'warn',
            summary: summary || '경고',
            detail: message,
            life: DEFAULT_TOAST_LIFE_MS.warn
        });
    };

    /**
     * 에러 메시지 표시
     */
    const showError = (message: string, summary?: string) => {
        toast.add({
            severity: 'error',
            summary: summary || '오류',
            detail: message,
            life: DEFAULT_TOAST_LIFE_MS.error
        });
    };

    /**
     * 커스텀 Toast 메시지 표시
     * @param life 밀리초. 생략 시 severity별 기본값. 0이면 자동 닫힘 없음(PrimeVue 동작).
     */
    const show = (severity: ToastSeverity, message: string, summary?: string, life?: number) => {
        const resolvedLife = life !== undefined ? life : DEFAULT_TOAST_LIFE_MS[severity];
        toast.add({
            severity,
            summary: summary || (severity === 'success' ? '성공' : severity === 'error' ? '오류' : severity === 'warn' ? '경고' : '알림'),
            detail: message,
            life: resolvedLife
        });
    };

    return {
        showSuccess,
        showInfo,
        showWarn,
        showError,
        show,
        toast // 원본 toast 인스턴스도 필요시 사용 가능
    };
};
