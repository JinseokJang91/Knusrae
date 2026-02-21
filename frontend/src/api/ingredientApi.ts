import { httpJson, httpForm } from '@/utils/http';
import { getApiBaseUrl } from '@/utils/constants';
import type { IngredientGroup, Ingredient, IngredientStorage, IngredientPreparation, IngredientRequest, IngredientRequestResponse } from '@/types/ingredient';

const BASE_URL = getApiBaseUrl('cook');

export interface IngredientListResponse {
    groups: IngredientGroup[];
    ingredients: Ingredient[];
    totalCount: number;
}

/**
 * 재료 그룹 목록 조회
 */
export async function getIngredientGroups(): Promise<IngredientGroup[]> {
    const response = await httpJson<{ groups?: IngredientGroup[] }>(BASE_URL, '/api/ingredients/groups', { method: 'GET' });
    return response.groups || [];
}

/**
 * 재료 목록 조회
 * @param params.type 'storage' = 보관법 등록된 재료만, 'preparation' = 손질법 등록된 재료만, 미지정 = 전체
 */
export async function getIngredients(params?: { groupId?: number; searchQuery?: string; type?: 'storage' | 'preparation'; limit?: number; offset?: number }): Promise<IngredientListResponse> {
    const queryParams = new URLSearchParams();
    if (params?.groupId) queryParams.append('groupId', params.groupId.toString());
    if (params?.searchQuery) queryParams.append('searchQuery', params.searchQuery);
    if (params?.type) queryParams.append('type', params.type);
    if (params?.limit) queryParams.append('limit', params.limit.toString());
    if (params?.offset) queryParams.append('offset', params.offset.toString());

    const response = await httpJson<IngredientListResponse>(BASE_URL, `/api/ingredients?${queryParams}`, { method: 'GET' });
    return {
        groups: response.groups || [],
        ingredients: response.ingredients || [],
        totalCount: response.totalCount || 0
    };
}

/**
 * 재료 보관법 조회
 */
export async function getIngredientStorage(ingredientId: number): Promise<IngredientStorage> {
    const response = await httpJson<{ data: IngredientStorage }>(BASE_URL, `/api/ingredients/${ingredientId}/storage`, { method: 'GET' });
    return response.data;
}

/**
 * 재료 손질법 조회
 */
export async function getIngredientPreparation(ingredientId: number): Promise<IngredientPreparation> {
    const response = await httpJson<{ data: IngredientPreparation }>(BASE_URL, `/api/ingredients/${ingredientId}/preparation`, { method: 'GET' });
    return response.data;
}

/**
 * 재료 정보 요청 생성
 */
export async function createIngredientRequest(request: IngredientRequest): Promise<IngredientRequestResponse> {
    const response = await httpJson<{ data: IngredientRequestResponse }>(BASE_URL, '/api/ingredients/requests', {
        method: 'POST',
        body: JSON.stringify(request)
    });
    return response.data;
}

/**
 * 사용자의 요청 목록 조회
 */
export async function getMyIngredientRequests(): Promise<IngredientRequestResponse[]> {
    const response = await httpJson<{ requests?: IngredientRequestResponse[] }>(BASE_URL, '/api/ingredients/requests/my', { method: 'GET' });
    return response.requests || [];
}

export interface AdminIngredientRequestsResponse {
    requests: IngredientRequestResponse[];
    totalCount: number;
    totalPages: number;
    currentPage: number;
}

/**
 * 관리자용 재료 정보 요청 목록 조회
 */
export async function getAdminIngredientRequests(params?: { page?: number; size?: number; status?: string }): Promise<AdminIngredientRequestsResponse> {
    const queryParams = new URLSearchParams();
    if (params?.page != null) queryParams.append('page', params.page.toString());
    if (params?.size != null) queryParams.append('size', params.size.toString());
    if (params?.status) queryParams.append('status', params.status);

    const response = await httpJson<AdminIngredientRequestsResponse>(BASE_URL, `/api/ingredients/requests/admin?${queryParams}`, { method: 'GET' });
    return {
        requests: response.requests || [],
        totalCount: response.totalCount ?? 0,
        totalPages: response.totalPages ?? 0,
        currentPage: response.currentPage ?? 0
    };
}

/**
 * 재료 정보 요청 상태 업데이트 (관리자 전용)
 */
export async function updateIngredientRequestStatus(requestId: number, status: string): Promise<IngredientRequestResponse> {
    const response = await httpJson<{ data: IngredientRequestResponse }>(BASE_URL, `/api/ingredients/requests/${requestId}/status`, {
        method: 'PUT',
        body: JSON.stringify({ status })
    });
    return response.data;
}

/**
 * 재료 그룹 등록 (관리자 전용)
 */
export async function createIngredientGroup(request: { name: string; imageUrl?: string; sortOrder?: number }): Promise<IngredientGroup> {
    const response = await httpJson<{ data: IngredientGroup }>(BASE_URL, '/api/admin/ingredients/groups', {
        method: 'POST',
        body: JSON.stringify(request)
    });
    return response.data;
}

/**
 * 재료 등록 (관리자 전용)
 */
export async function createIngredient(request: { groupId: number; name: string; imageUrl?: string; sortOrder?: number }): Promise<Ingredient> {
    const response = await httpJson<{ data: Ingredient }>(BASE_URL, '/api/admin/ingredients', {
        method: 'POST',
        body: JSON.stringify(request)
    });
    return response.data;
}

/**
 * 재료 그룹 수정 (관리자 전용)
 */
export async function updateIngredientGroup(groupId: number, request: { name: string; imageUrl?: string; sortOrder?: number }): Promise<IngredientGroup> {
    const response = await httpJson<{ data: IngredientGroup }>(BASE_URL, `/api/admin/ingredients/groups/${groupId}`, { method: 'PUT', body: JSON.stringify(request) });
    return response.data;
}

/**
 * 재료 그룹 삭제 (관리자 전용, 하위 재료 함께 삭제)
 */
export async function deleteIngredientGroup(groupId: number): Promise<void> {
    await httpJson(BASE_URL, `/api/admin/ingredients/groups/${groupId}`, {
        method: 'DELETE'
    });
}

/**
 * 재료 수정 (관리자 전용)
 */
export async function updateIngredient(ingredientId: number, request: { groupId: number; name: string; imageUrl?: string; sortOrder?: number }): Promise<Ingredient> {
    const response = await httpJson<{ data: Ingredient }>(BASE_URL, `/api/admin/ingredients/${ingredientId}`, { method: 'PUT', body: JSON.stringify(request) });
    return response.data;
}

/**
 * 재료 삭제 (관리자 전용)
 */
export async function deleteIngredient(ingredientId: number): Promise<void> {
    await httpJson(BASE_URL, `/api/admin/ingredients/${ingredientId}`, {
        method: 'DELETE'
    });
}

/**
 * 재료 보관법 등록 (관리자 전용)
 */
export async function createIngredientStorage(request: { ingredientId: number; content: string; summary?: string }): Promise<IngredientStorage> {
    const response = await httpJson<{ data: IngredientStorage }>(BASE_URL, '/api/admin/ingredients/storage', {
        method: 'POST',
        body: JSON.stringify(request)
    });
    return response.data;
}

/**
 * 재료 손질법 등록 (관리자 전용)
 */
export async function createIngredientPreparation(request: { ingredientId: number; content: string; summary?: string }): Promise<IngredientPreparation> {
    const response = await httpJson<{ data: IngredientPreparation }>(BASE_URL, '/api/admin/ingredients/preparation', {
        method: 'POST',
        body: JSON.stringify(request)
    });
    return response.data;
}

/**
 * 본문용 이미지 업로드 (에디터 addImageBlobHook, 관리자 전용)
 */
export async function uploadContentImage(file: File): Promise<{ url: string }> {
    const formData = new FormData();
    formData.append('image', file);
    const res = await httpForm(BASE_URL, '/api/admin/ingredients/upload-image', formData);
    return res as { url: string };
}
