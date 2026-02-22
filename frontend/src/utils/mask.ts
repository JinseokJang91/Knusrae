/**
 * 화면 표시용 개인정보 마스킹 유틸
 * (백엔드 API가 이미 마스킹된 값을 내려주더라도, 다른 소스 표시 시 재사용 가능)
 */

/** 이메일 마스킹 (앞 2자 + *** + @ 이후 도메인) */
export function maskEmail(email: string | undefined | null): string {
    if (!email || !email.includes('@')) return '';
    const [local, domain] = email.split('@');
    if (!domain) return '***';
    if (local.length <= 2) return `${local}***@${domain}`;
    return `${local.slice(0, 2)}***@${domain}`;
}

/** 전화번호 마스킹 (010-****-5678) */
export function maskPhone(phone: string | undefined | null): string {
    if (!phone) return '';
    const digits = phone.replace(/\D/g, '');
    if (digits.length < 4) return '***';
    return `${digits.slice(0, 3)}-****-${digits.slice(-4)}`;
}
