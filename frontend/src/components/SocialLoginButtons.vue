<script setup lang="ts">
type Provider = 'kakao' | 'google' | 'naver';

defineProps<{
    disabled?: boolean;
    compactDesktop?: boolean;
}>();

defineEmits<{
    (e: 'click', provider: Provider): void;
}>();

const providers: Array<{
    key: Provider;
    label: string;
    ariaLabel: string;
}> = [
    { key: 'kakao', label: '카카오 로그인', ariaLabel: '카카오 로그인' },
    { key: 'google', label: 'Sign in with Google', ariaLabel: 'Sign in with Google' },
    { key: 'naver', label: '네이버 로그인', ariaLabel: '네이버 로그인' }
];
</script>

<template>
    <div class="social-login" :class="{ 'social-login--compact-desktop': compactDesktop }">
        <button v-for="p in providers" :key="p.key" type="button" class="social-btn" :class="`social-btn--${p.key}`" :disabled="disabled" :aria-label="p.ariaLabel" @click="$emit('click', p.key)">
            <!-- 아이콘: 왼쪽 절대 위치 고정 -->
            <span class="social-btn__icon" aria-hidden="true">
                <!-- 카카오 말풍선 심볼: viewBox를 경로에 맞게 크롭하여 실제 크기 균일화 -->
                <svg v-if="p.key === 'kakao'" viewBox="2 3 20 19" fill="#000000">
                    <path d="M12 3C6.48 3 2 6.67 2 11.18c0 2.8 1.6 5.27 4.07 6.82L5.16 21.5l4.2-2.27c.84.17 1.7.25 2.64.25 5.52 0 10-3.67 10-8.3C22 6.67 17.52 3 12 3z" />
                </svg>

                <!-- 구글 G 로고: 공식 브랜드 컬러, 배경·테두리 없음 -->
                <svg v-else-if="p.key === 'google'" viewBox="10 10 20 20" fill="none">
                    <path d="M29.6 20.2273C29.6 19.5182 29.5364 18.8364 29.4182 18.1818H20V22.05H25.3818C25.15 23.3 24.4455 24.3591 23.3864 25.0682V27.5773H26.6182C28.5091 25.8364 29.6 23.2727 29.6 20.2273Z" fill="#4285F4" />
                    <path d="M20 30C22.7 30 24.9636 29.1045 26.6181 27.5773L23.3863 25.0682C22.4909 25.6682 21.3454 26.0227 20 26.0227C17.3954 26.0227 15.1909 24.2636 14.4045 21.9H11.0636V24.4909C12.7091 27.7591 16.0909 30 20 30Z" fill="#34A853" />
                    <path d="M14.4045 21.9C14.2045 21.3 14.0909 20.6591 14.0909 20C14.0909 19.3409 14.2045 18.7 14.4045 18.1V15.5091H11.0636C10.3864 16.8591 10 18.3864 10 20C10 21.6136 10.3864 23.1409 11.0636 24.4909L14.4045 21.9Z" fill="#FBBC04" />
                    <path
                        d="M20 13.9773C21.4681 13.9773 22.7863 14.4818 23.8227 15.4727L26.6909 12.6045C24.9591 10.9909 22.6954 10 20 10C16.0909 10 12.7091 12.2409 11.0636 15.5091L14.4045 18.1C15.1909 15.7364 17.3954 13.9773 20 13.9773Z"
                        fill="#E94235"
                    />
                </svg>

                <!-- 네이버 N 로고: currentColor → white -->
                <svg v-else-if="p.key === 'naver'" viewBox="0 0 24 24" fill="currentColor">
                    <path d="M16.273 12.845L7.376 0H0v24h7.727V11.155L16.624 24H24V0h-7.727z" />
                </svg>
            </span>

            <span class="social-btn__label">{{ p.label }}</span>
        </button>
    </div>
</template>

<style scoped>
.social-login {
    display: grid;
    gap: 12px;
}

.social-btn {
    width: 100%;
    height: 48px;
    border-radius: 12px;
    padding: 0 16px;

    display: flex;
    align-items: center;
    /* gap을 padding-right와 동일하게 설정 → 텍스트가 아이콘~우측 끝 사이 정중앙에 위치 */
    gap: 16px;

    font-size: 16px;
    line-height: 1;

    border: 1px solid transparent;
    cursor: pointer;
    user-select: none;

    transition:
        transform 0.02s ease,
        opacity 0.15s ease;
}

.social-btn:active {
    transform: scale(0.99);
}

.social-btn:disabled {
    opacity: 0.5;
    cursor: not-allowed;
}

.social-btn:focus-visible {
    outline: 2px solid currentColor;
    outline-offset: 2px;
}

.social-btn__icon {
    flex: 0 0 22px;
    width: 22px;
    height: 22px;
    display: flex;
    align-items: center;
    justify-content: center;
}

.social-btn__icon svg {
    width: 100%;
    height: 100%;
}

/* flex: 1 + text-align: center → gap === padding-right 조건에서 완전 중앙 정렬 */
.social-btn__label {
    flex: 1;
    text-align: center;
    white-space: nowrap;
}

/* 브랜드 스킨 */
.social-btn--kakao {
    background: #fee500;
    color: rgba(0, 0, 0, 0.85);
}

.social-btn--naver {
    background: #03c75a;
    color: #ffffff;
}

.social-btn--google {
    background: #ffffff;
    color: #1f1f1f;
    border-color: #dadce0;
    font-family: 'Roboto', 'Noto Sans KR', sans-serif;
    font-weight: 500;
}

/* 네이버 완성형 가이드: 가운데 정렬 시 아이콘-레이블 간격 8px 권장 */
.social-btn--naver {
    gap: 8px;
}

/* 카카오 가이드: 컨테이너 코너 라운드 12px */
.social-btn--kakao {
    border-radius: 12px;
}

@media (min-width: 1024px) {
    .social-login--compact-desktop .social-btn {
        height: 42px;
        border-radius: 12px;
        padding: 0 12px;
        gap: 12px;
        font-size: 16px;
    }

    .social-login--compact-desktop .social-btn--naver {
        gap: 8px;
    }

    .social-login--compact-desktop .social-btn__icon {
        flex-basis: 22px;
        width: 22px;
        height: 22px;
    }
}
</style>
