/**
 * 백엔드 API 에러 응답 스펙 (프론트 공통 가정)
 * - HTTP 4xx/5xx 시 응답 본문은 JSON 문자열로 전달되며, 에러 메시지 추출 시 아래 필드를 사용합니다.
 * - 우선 사용: body.message (문자열)
 * - 없을 경우: 본문 전체 문자열 → 호출부 defaultMessage 순으로 fallback
 */
export interface ApiErrorBody {
    message?: string;
}

/**
 * 에러 메시지 추출 (fallback 순서 명시)
 * 1. err가 Error가 아니면 → defaultMessage
 * 2. err.message가 "HTTP 숫자: {...}" 형태면:
 *    - JSON 파싱 성공 시: errorBody.message → 없으면 본문 문자열 → 없으면 defaultMessage
 *    - JSON 파싱 실패 시: 본문 문자열 → 없으면 defaultMessage
 * 3. 그 외: err.message → 없으면 defaultMessage
 */
export function extractErrorMessage(err: unknown, defaultMessage: string): string {
    if (!(err instanceof Error)) {
        return defaultMessage;
    }

    const httpErrorMatch = err.message.match(/HTTP \d+: (.+)/);
    if (httpErrorMatch) {
        const rawBody = httpErrorMatch[1];
        try {
            const errorBody = JSON.parse(rawBody) as ApiErrorBody;
            return errorBody.message ?? rawBody ?? defaultMessage;
        } catch {
            return rawBody || defaultMessage;
        }
    }

    return err.message || defaultMessage;
}

export interface ErrorHandlerOptions {
    /** 에러 토스트 제목 (기본값: '오류') */
    errorSummary?: string;
    /** 실패 시 토스트 표시 여부. true이면 showError 콜백으로 사용자 피드백 표시 (호출처에서 useAppToast().showError 전달 권장) */
    showToast?: boolean;
    /** 실패 시 호출할 콜백 (message, summary). showToast 사용 시 컴포넌트에서 useAppToast().showError 전달 */
    showError?: (message: string, summary?: string) => void;
}

/**
 * 에러 처리 Composable
 * 컴포넌트 내부에서 사용하여 API 호출 에러를 일관되게 처리합니다.
 * 사용자 피드백을 위해 useErrorHandler({ showError: useAppToast().showError }) 형태로 showError를 넘기면
 * API 실패 시 토스트가 표시됩니다.
 */
export function useErrorHandler(opts?: ErrorHandlerOptions) {
    const showToastDefault = opts?.showToast !== false;
    const showErrorFn = opts?.showError;

    /**
     * API 호출 에러 처리 유틸리티
     * @param apiCall API 호출 함수
     * @param errorMessage 에러 발생 시 사용자에게 표시할 메시지
     * @param errorSummaryOrOpts 에러 토스트 제목 또는 옵션 (기본값: '오류')
     * @returns 성공 시 데이터, 실패 시 null
     */
    function handleApiCall<T>(apiCall: () => Promise<T>, errorMessage: string, errorSummaryOrOpts: string | ErrorHandlerOptions = '오류'): Promise<T | null> {
        const summary = typeof errorSummaryOrOpts === 'string' ? errorSummaryOrOpts : (errorSummaryOrOpts.errorSummary ?? '오류');
        const showToast = typeof errorSummaryOrOpts === 'object' && errorSummaryOrOpts.showToast !== undefined ? errorSummaryOrOpts.showToast : showToastDefault;
        return (async () => {
            try {
                return await apiCall();
            } catch (err) {
                console.error(errorMessage, err);
                const detailMessage = extractErrorMessage(err, errorMessage);
                console.error(`${summary}: ${detailMessage}`);
                if (showToast && showErrorFn) {
                    showErrorFn(detailMessage, summary);
                }
                return null;
            }
        })();
    }

    /**
     * API 호출 에러 처리 (void 반환용)
     * @param apiCall API 호출 함수
     * @param errorMessage 에러 발생 시 사용자에게 표시할 메시지
     * @param errorSummaryOrOpts 에러 토스트 제목 또는 옵션 (기본값: '오류')
     * @returns 성공 시 true, 실패 시 false
     */
    async function handleApiCallVoid(apiCall: () => Promise<void>, errorMessage: string, errorSummaryOrOpts: string | ErrorHandlerOptions = '오류'): Promise<boolean> {
        const summary = typeof errorSummaryOrOpts === 'string' ? errorSummaryOrOpts : (errorSummaryOrOpts.errorSummary ?? '오류');
        const showToast = typeof errorSummaryOrOpts === 'object' && errorSummaryOrOpts.showToast !== undefined ? errorSummaryOrOpts.showToast : showToastDefault;
        try {
            await apiCall();
            return true;
        } catch (err) {
            console.error(errorMessage, err);
            const detailMessage = extractErrorMessage(err, errorMessage);
            console.error(`${summary}: ${detailMessage}`);
            if (showToast && showErrorFn) {
                showErrorFn(detailMessage, summary);
            }
            return false;
        }
    }

    return {
        handleApiCall,
        handleApiCallVoid,
        extractErrorMessage
    };
}
