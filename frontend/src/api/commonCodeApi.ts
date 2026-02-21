import { httpJson } from '@/utils/http';
import { getApiBaseUrl } from '@/utils/constants';
import type { CommonCodeOption } from '@/types/recipeForm';
import type { CommonCodeListItem, AdminCommonCode, AdminCommonCodeDetailItem, CommonCodeCreateRequest, CommonCodeUpdateRequest, CommonCodeDetailCreateRequest, CommonCodeDetailUpdateRequest } from '@/types/common';

const BASE_URL = getApiBaseUrl('cook');

/**
 * codeGroup으로 공통코드 목록 조회
 * @param codeGroup 예: CATEGORY, COOKINGTIP
 */
export async function getCommonCodesByGroup(codeGroup: string): Promise<CommonCodeOption[]> {
    const response = await httpJson<CommonCodeOption[] | unknown>(BASE_URL, `/api/common-codes?codeGroup=${encodeURIComponent(codeGroup)}`, { method: 'GET' });
    return Array.isArray(response) ? response : [];
}

/**
 * codeId로 공통코드 목록 조회
 * @param codeId 예: INGREDIENTS_GROUP, INGREDIENTS_UNIT
 */
export async function getCommonCodesByCodeId(codeId: string): Promise<CommonCodeOption[]> {
    const response = await httpJson<CommonCodeOption[] | unknown>(BASE_URL, `/api/common-codes?codeId=${encodeURIComponent(codeId)}`, { method: 'GET' });
    return Array.isArray(response) ? response : [];
}

// ---------- 관리자 공통코드 CRUD ----------

/** 관리자: 전체 공통코드 목록 */
export async function getAdminCommonCodeList(): Promise<CommonCodeListItem[]> {
    const response = await httpJson<CommonCodeListItem[] | unknown>(BASE_URL, '/api/admin/common-codes', { method: 'GET' });
    return Array.isArray(response) ? response : [];
}

/** 관리자: 공통코드 단건 조회 (상세 포함) */
export async function getAdminCommonCodeOne(codeId: string): Promise<AdminCommonCode> {
    return httpJson<AdminCommonCode>(BASE_URL, `/api/admin/common-codes/${encodeURIComponent(codeId)}`, { method: 'GET' });
}

/** 관리자: 공통코드 생성 */
export async function createCommonCode(request: CommonCodeCreateRequest): Promise<CommonCodeListItem> {
    return httpJson<CommonCodeListItem>(BASE_URL, '/api/admin/common-codes', {
        method: 'POST',
        body: JSON.stringify(request)
    });
}

/** 관리자: 공통코드 수정 */
export async function updateCommonCode(codeId: string, request: CommonCodeUpdateRequest): Promise<AdminCommonCode> {
    return httpJson<AdminCommonCode>(BASE_URL, `/api/admin/common-codes/${encodeURIComponent(codeId)}`, {
        method: 'PUT',
        body: JSON.stringify(request)
    });
}

/** 관리자: 공통코드 삭제 */
export async function deleteCommonCode(codeId: string): Promise<void> {
    await httpJson(BASE_URL, `/api/admin/common-codes/${encodeURIComponent(codeId)}`, { method: 'DELETE' });
}

// ---------- 관리자 상세코드 CRUD ----------

/** 관리자: 상세코드 추가 */
export async function createCommonCodeDetail(codeId: string, request: CommonCodeDetailCreateRequest): Promise<AdminCommonCodeDetailItem> {
    return httpJson<AdminCommonCodeDetailItem>(BASE_URL, `/api/admin/common-codes/${encodeURIComponent(codeId)}/details`, {
        method: 'POST',
        body: JSON.stringify(request)
    });
}

/** 관리자: 상세코드 수정 */
export async function updateCommonCodeDetail(codeId: string, detailCodeId: string, request: CommonCodeDetailUpdateRequest): Promise<AdminCommonCodeDetailItem> {
    return httpJson<AdminCommonCodeDetailItem>(BASE_URL, `/api/admin/common-codes/${encodeURIComponent(codeId)}/details/${encodeURIComponent(detailCodeId)}`, {
        method: 'PUT',
        body: JSON.stringify(request)
    });
}

/** 관리자: 상세코드 삭제 */
export async function deleteCommonCodeDetail(codeId: string, detailCodeId: string): Promise<void> {
    await httpJson(BASE_URL, `/api/admin/common-codes/${encodeURIComponent(codeId)}/details/${encodeURIComponent(detailCodeId)}`, { method: 'DELETE' });
}
