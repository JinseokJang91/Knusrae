import { useToast } from 'primevue/usetoast';

/**
 * 에러 메시지 추출 유틸리티
 * @param err 에러 객체
 * @param defaultMessage 기본 에러 메시지
 * @returns 추출된 에러 메시지
 */
function extractErrorMessage(err: unknown, defaultMessage: string): string {
    if (!(err instanceof Error)) {
        return defaultMessage;
    }

    // HTTP 에러 메시지에서 의미있는 부분만 추출
    const httpErrorMatch = err.message.match(/HTTP \d+: (.+)/);
    if (httpErrorMatch) {
        try {
            const errorBody = JSON.parse(httpErrorMatch[1]);
            return errorBody.message || httpErrorMatch[1] || defaultMessage;
        } catch {
            return httpErrorMatch[1] || defaultMessage;
        }
    }

    return err.message || defaultMessage;
}

/**
 * 에러 처리 Composable
 * 컴포넌트 내부에서 사용하여 API 호출 에러를 일관되게 처리합니다.
 */
export function useErrorHandler() {
    const toast = useToast();

    /**
     * 에러 토스트 표시
     * @param detailMessage 표시할 상세 메시지
     * @param summary 토스트 제목 (기본값: '오류')
     */
    const showErrorToast = (detailMessage: string, summary: string = '오류'): void => {
        toast.add({
            severity: 'error',
            summary,
            detail: detailMessage,
            life: 3000
        });
    };

    /**
     * API 호출 에러 처리 유틸리티
     * @param apiCall API 호출 함수
     * @param errorMessage 에러 발생 시 사용자에게 표시할 메시지
     * @param errorSummary 에러 토스트 제목 (기본값: '오류')
     * @returns 성공 시 데이터, 실패 시 null
     */
    function handleApiCall<T>(
        apiCall: () => Promise<T>,
        errorMessage: string,
        errorSummary: string = '오류'
    ): Promise<T | null> {
        return (async () => {
            try {
                return await apiCall();
            } catch (err) {
                console.error(errorMessage, err);
                const detailMessage = extractErrorMessage(err, errorMessage);
                showErrorToast(detailMessage, errorSummary);
                return null;
            }
        })();
    }

    /**
     * API 호출 에러 처리 (void 반환용)
     * @param apiCall API 호출 함수
     * @param errorMessage 에러 발생 시 사용자에게 표시할 메시지
     * @param errorSummary 에러 토스트 제목 (기본값: '오류')
     * @returns 성공 시 true, 실패 시 false
     */
    async function handleApiCallVoid(
        apiCall: () => Promise<void>,
        errorMessage: string,
        errorSummary: string = '오류'
    ): Promise<boolean> {
        try {
            await apiCall();
            return true;
        } catch (err) {
            console.error(errorMessage, err);
            const detailMessage = extractErrorMessage(err, errorMessage);
            showErrorToast(detailMessage, errorSummary);
            return false;
        }
    };

    return {
        handleApiCall,
        handleApiCallVoid,
        showErrorToast,
        extractErrorMessage
    };
}

