import { useToast } from 'primevue/usetoast';

/**
 * API 호출 에러 처리 유틸리티
 * @param apiCall API 호출 함수
 * @param errorMessage 에러 발생 시 사용자에게 표시할 메시지
 * @returns 성공 시 데이터, 실패 시 null
 */
export async function handleApiCall<T>(
    apiCall: () => Promise<T>,
    errorMessage: string
): Promise<T | null> {
    const toast = useToast();
    
    try {
        return await apiCall();
    } catch (err) {
        console.error(errorMessage, err);
        
        // 에러 메시지 추출
        let detailMessage = errorMessage;
        if (err instanceof Error) {
            // HTTP 에러 메시지에서 의미있는 부분만 추출
            const httpErrorMatch = err.message.match(/HTTP \d+: (.+)/);
            if (httpErrorMatch) {
                try {
                    const errorBody = JSON.parse(httpErrorMatch[1]);
                    detailMessage = errorBody.message || errorMessage;
                } catch {
                    detailMessage = httpErrorMatch[1] || errorMessage;
                }
            } else {
                detailMessage = err.message || errorMessage;
            }
        }
        
        toast.add({
            severity: 'error',
            summary: '오류',
            detail: detailMessage,
            life: 3000
        });
        
        return null;
    }
}

/**
 * API 호출 에러 처리 (void 반환용)
 * @param apiCall API 호출 함수
 * @param errorMessage 에러 발생 시 사용자에게 표시할 메시지
 * @returns 성공 시 true, 실패 시 false
 */
export async function handleApiCallVoid(
    apiCall: () => Promise<void>,
    errorMessage: string
): Promise<boolean> {
    const toast = useToast();
    
    try {
        await apiCall();
        return true;
    } catch (err) {
        console.error(errorMessage, err);
        
        let detailMessage = errorMessage;
        if (err instanceof Error) {
            const httpErrorMatch = err.message.match(/HTTP \d+: (.+)/);
            if (httpErrorMatch) {
                try {
                    const errorBody = JSON.parse(httpErrorMatch[1]);
                    detailMessage = errorBody.message || errorMessage;
                } catch {
                    detailMessage = httpErrorMatch[1] || errorMessage;
                }
            } else {
                detailMessage = err.message || errorMessage;
            }
        }
        
        toast.add({
            severity: 'error',
            summary: '오류',
            detail: detailMessage,
            life: 3000
        });
        
        return false;
    }
}

