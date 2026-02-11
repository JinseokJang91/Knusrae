// 문의 유형 코드 (백엔드와 동일)
export type InquiryType =
    | 'RECIPE_CONTENT'
    | 'TECHNICAL'
    | 'ACCOUNT'
    | 'REPORT'
    | 'ETC';

export const INQUIRY_TYPES: { value: InquiryType; label: string }[] = [
    { value: 'RECIPE_CONTENT', label: '레시피/콘텐츠' },
    { value: 'TECHNICAL', label: '이용/기술 문제' },
    { value: 'ACCOUNT', label: '계정' },
    { value: 'REPORT', label: '신고' },
    { value: 'ETC', label: '기타' }
];

export function getInquiryTypeLabel(value: InquiryType | string): string {
    return INQUIRY_TYPES.find((t) => t.value === value)?.label ?? value;
}

// 목록 항목
export interface InquiryListItem {
    id: number;
    memberId?: number;
    inquiryType: string;
    title: string;
    hasReply: boolean;
    createdAt: string;
}

// 목록 응답 (페이징)
export interface InquiryListResponse {
    content: InquiryListItem[];
    currentPage: number;
    totalPages: number;
    totalElements: number;
}

// 관리자 답변
export interface InquiryReplyDto {
    id: number;
    content: string;
    replyBy: number | null;
    repliedAt: string;
}

// 상세
export interface InquiryDetail {
    id: number;
    memberId: number;
    inquiryType: string;
    title: string;
    content: string;
    createdAt: string;
    updatedAt: string;
    imageUrls: string[];
    reply: InquiryReplyDto | null;
}

// 폼 (작성/수정)
export interface InquiryForm {
    title: string;
    inquiryType: InquiryType | '';
    content: string;
    images: File[];
}
