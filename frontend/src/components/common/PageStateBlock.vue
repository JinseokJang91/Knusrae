<script setup lang="ts">
import Button from 'primevue/button';
import ProgressSpinner from 'primevue/progressspinner';

defineProps<{
    /** 'loading' | 'error' | 'empty' */
    state: 'loading' | 'error' | 'empty';
    /** 마이페이지 등: 모바일에서 빈 상태 UI 타이포·버튼 축소 */
    compactMobile?: boolean;
    /** 로딩 시 문구 */
    loadingMessage?: string;
    /** 에러 제목 */
    errorTitle?: string;
    /** 에러 메시지 */
    errorMessage?: string;
    /** 재시도 버튼 라벨 */
    retryLabel?: string;
    /** 빈 상태 아이콘 (PrimeIcons 클래스, 예: 'pi pi-book') */
    emptyIcon?: string;
    /** 빈 상태 제목 */
    emptyTitle?: string;
    /** 빈 상태 설명 */
    emptyMessage?: string;
    /** 빈 상태 액션 버튼 라벨 */
    emptyButtonLabel?: string;
}>();

defineEmits<{
    retry: [];
    'empty-action': [];
}>();
</script>

<template>
    <div class="page-state-block text-center py-8" :class="{ 'page-state-block--compact-mobile': compactMobile }">
        <!-- 로딩 상태 -->
        <template v-if="state === 'loading'">
            <ProgressSpinner />
            <p class="page-state-block__message text-gray-600 mt-3">{{ loadingMessage }}</p>
        </template>

        <!-- 에러 상태 -->
        <template v-else-if="state === 'error'">
            <i class="pi pi-exclamation-triangle page-state-block__icon page-state-block__icon--error text-6xl text-red-500 mb-4"></i>
            <h3 class="page-state-block__title text-2xl font-semibold text-gray-600 mb-2">{{ errorTitle }}</h3>
            <p class="page-state-block__message text-gray-600 mb-4">{{ errorMessage }}</p>
            <Button v-if="retryLabel" class="page-state-block__action" :label="retryLabel" @click="$emit('retry')" />
        </template>

        <!-- 빈 상태 -->
        <template v-else-if="state === 'empty'">
            <i :class="[emptyIcon, 'page-state-block__icon text-6xl text-300 mb-4']"></i>
            <h3 class="page-state-block__title text-2xl font-semibold text-gray-600 mb-2">{{ emptyTitle }}</h3>
            <p class="page-state-block__message text-gray-600 mb-4">{{ emptyMessage }}</p>
            <Button v-if="emptyButtonLabel" class="page-state-block__action" :label="emptyButtonLabel" @click="$emit('empty-action')" />
        </template>
    </div>
</template>

<style scoped>
.page-state-block {
    min-height: 200px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
}

@media (max-width: 767px) {
    .page-state-block--compact-mobile {
        min-height: 160px;
        padding-top: 1.5rem;
        padding-bottom: 1.5rem;
    }

    .page-state-block--compact-mobile .page-state-block__icon {
        font-size: 2.5rem !important;
        margin-bottom: 0.75rem !important;
    }

    .page-state-block--compact-mobile .page-state-block__title {
        font-size: 1rem;
        line-height: 1.35;
        margin-bottom: 0.375rem;
    }

    .page-state-block--compact-mobile .page-state-block__message {
        font-size: 0.8125rem;
        line-height: 1.45;
        margin-bottom: 0.75rem;
    }

    .page-state-block--compact-mobile :deep(.page-state-block__action.p-button) {
        min-height: 0;
        padding: 0.4375rem 0.75rem;
        font-size: 0.8125rem;
        line-height: 1.25;
    }

    .page-state-block--compact-mobile :deep(.page-state-block__action .p-button-label) {
        line-height: 1.25;
    }
}

@media (max-width: 480px) {
    .page-state-block--compact-mobile .page-state-block__icon {
        font-size: 2.25rem !important;
    }

    .page-state-block--compact-mobile .page-state-block__title {
        font-size: 0.9375rem;
    }

    .page-state-block--compact-mobile .page-state-block__message {
        font-size: 0.75rem;
    }

    .page-state-block--compact-mobile :deep(.page-state-block__action.p-button) {
        padding: 0.375rem 0.625rem;
        font-size: 0.75rem;
    }
}
</style>
