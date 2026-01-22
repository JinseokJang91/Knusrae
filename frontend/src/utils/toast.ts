import { useToast } from 'primevue/usetoast';

export type ToastSeverity = 'success' | 'info' | 'warn' | 'error';

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
            life: 3000
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
            life: 3000
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
            life: 3000
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
            life: 4000
        });
    };

    /**
     * 커스텀 Toast 메시지 표시
     */
    const show = (severity: ToastSeverity, message: string, summary?: string, life?: number) => {
        toast.add({
            severity,
            summary: summary || (severity === 'success' ? '성공' : severity === 'error' ? '오류' : severity === 'warn' ? '경고' : '알림'),
            detail: message,
            life: life || 3000
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

