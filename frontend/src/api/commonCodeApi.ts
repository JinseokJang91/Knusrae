import { httpJson } from '@/utils/http';
import { getApiBaseUrl } from '@/utils/constants';
import type { CommonCodeOption } from '@/types/recipeForm';

const BASE_URL = getApiBaseUrl('cook');

/**
 * codeGroup으로 공통코드 목록 조회
 * @param codeGroup 예: CATEGORY, COOKINGTIP
 */
export async function getCommonCodesByGroup(codeGroup: string): Promise<CommonCodeOption[]> {
    const response = await httpJson<CommonCodeOption[] | unknown>(
        BASE_URL,
        `/api/common-codes?codeGroup=${encodeURIComponent(codeGroup)}`,
        { method: 'GET' }
    );
    return Array.isArray(response) ? response : [];
}

/**
 * codeId로 공통코드 목록 조회
 * @param codeId 예: INGREDIENTS_GROUP, INGREDIENTS_UNIT
 */
export async function getCommonCodesByCodeId(codeId: string): Promise<CommonCodeOption[]> {
    const response = await httpJson<CommonCodeOption[] | unknown>(
        BASE_URL,
        `/api/common-codes?codeId=${encodeURIComponent(codeId)}`,
        { method: 'GET' }
    );
    return Array.isArray(response) ? response : [];
}
