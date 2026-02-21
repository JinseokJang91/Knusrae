import { httpJson, httpForm } from '@/utils/http';
import { getApiBaseUrl } from '@/utils/constants';
import type { InquiryListResponse, InquiryDetail } from '@/types/inquiry';

const BASE_URL = getApiBaseUrl('member');

/**
 * 내 문의 목록 조회 (페이징, 인증된 회원 기준)
 */
export async function getMyInquiries(page: number, size: number): Promise<InquiryListResponse> {
    return await httpJson<InquiryListResponse>(BASE_URL, `/api/inquiries/my?page=${page}&size=${size}`, {
        method: 'GET'
    });
}

/**
 * 문의 상세 조회
 */
export async function getInquiryDetail(id: number): Promise<InquiryDetail> {
    return await httpJson<InquiryDetail>(BASE_URL, `/api/inquiries/${id}`, { method: 'GET' });
}

/**
 * 문의 등록 (multipart: title, inquiryType, content, images)
 */
export async function createInquiry(formData: FormData): Promise<InquiryDetail> {
    return await httpForm<InquiryDetail>(BASE_URL, '/api/inquiries', formData, { method: 'POST' });
}

/**
 * 문의 수정 (답변 없을 때만 가능)
 */
export async function updateInquiry(id: number, formData: FormData): Promise<InquiryDetail> {
    return await httpForm<InquiryDetail>(BASE_URL, `/api/inquiries/${id}`, formData, { method: 'PUT' });
}

/**
 * 문의 삭제
 */
export async function deleteInquiry(id: number): Promise<void> {
    await httpJson(BASE_URL, `/api/inquiries/${id}`, { method: 'DELETE' });
}

// ---------- 관리자 API ----------

/**
 * 관리자: 전체 문의 목록 조회 (페이징)
 */
export async function getAdminInquiries(page: number, size: number): Promise<InquiryListResponse> {
    return await httpJson<InquiryListResponse>(BASE_URL, `/api/inquiries/admin?page=${page}&size=${size}`, { method: 'GET' });
}

/**
 * 관리자: 문의 상세 조회
 */
export async function getAdminInquiryDetail(id: number): Promise<InquiryDetail> {
    return await httpJson<InquiryDetail>(BASE_URL, `/api/inquiries/admin/${id}`, { method: 'GET' });
}

/**
 * 관리자: 문의 답변 등록
 */
export async function submitInquiryReply(id: number, content: string): Promise<InquiryDetail> {
    return await httpJson<InquiryDetail>(BASE_URL, `/api/inquiries/${id}/reply`, {
        method: 'POST',
        body: JSON.stringify({ content })
    });
}
