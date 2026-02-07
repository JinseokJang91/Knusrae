/**
 * API 응답 래퍼 타입
 * 백엔드가 { data: T } 형태로 감싸서 반환할 때 사용
 */
export interface ApiResponse<T> {
    data: T;
}

/** 관리자 공통코드 목록 항목 */
export interface CommonCodeListItem {
    codeId: string;
    codeGroup: string;
    codeName: string;
    useYn: string;
}

/** 관리자 공통코드 상세(하위) 항목 */
export interface AdminCommonCodeDetailItem {
    detailCodeId: string;
    codeName: string;
    sort: number | null;
    useYn: string;
}

/** 관리자 공통코드 상세 응답 (단건 조회/수정 응답) */
export interface AdminCommonCode {
    codeId: string;
    codeGroup: string;
    codeName: string;
    useYn: string;
    details: AdminCommonCodeDetailItem[];
}

/** 공통코드 생성 요청 */
export interface CommonCodeCreateRequest {
    codeId: string;
    codeGroup: string;
    codeName: string;
    useYn?: string;
}

/** 공통코드 수정 요청 */
export interface CommonCodeUpdateRequest {
    codeGroup?: string;
    codeName?: string;
    useYn?: string;
}

/** 상세코드 생성 요청 */
export interface CommonCodeDetailCreateRequest {
    detailCodeId: string;
    codeName: string;
    sort?: number | null;
    useYn?: string;
}

/** 상세코드 수정 요청 */
export interface CommonCodeDetailUpdateRequest {
    codeName?: string;
    sort?: number | null;
    useYn?: string;
}
