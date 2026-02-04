/**
 * API 응답 래퍼 타입
 * 백엔드가 { data: T } 형태로 감싸서 반환할 때 사용
 */
export interface ApiResponse<T> {
    data: T;
}
