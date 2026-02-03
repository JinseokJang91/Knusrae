<template>
    <div class="page-state-block text-center py-8">
        <!-- 로딩 상태 -->
        <template v-if="state === 'loading'">
            <ProgressSpinner />
            <p class="text-gray-600 mt-3">{{ loadingMessage }}</p>
        </template>

        <!-- 에러 상태 -->
        <template v-else-if="state === 'error'">
            <i class="pi pi-exclamation-triangle text-6xl text-red-500 mb-4"></i>
            <h3 class="text-2xl font-semibold text-gray-600 mb-2">{{ errorTitle }}</h3>
            <p class="text-gray-600 mb-4">{{ errorMessage }}</p>
            <Button v-if="retryLabel" :label="retryLabel" @click="$emit('retry')" />
        </template>

        <!-- 빈 상태 -->
        <template v-else-if="state === 'empty'">
            <i :class="emptyIcon" class="text-6xl text-300 mb-4"></i>
            <h3 class="text-2xl font-semibold text-gray-600 mb-2">{{ emptyTitle }}</h3>
            <p class="text-gray-600 mb-4">{{ emptyMessage }}</p>
            <Button v-if="emptyButtonLabel" :label="emptyButtonLabel" @click="$emit('empty-action')" />
        </template>
    </div>
</template>

<script setup lang="ts">
import Button from 'primevue/button';
import ProgressSpinner from 'primevue/progressspinner';

defineProps<{
    /** 'loading' | 'error' | 'empty' */
    state: 'loading' | 'error' | 'empty';
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

<style scoped>
.page-state-block {
    min-height: 200px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
}
</style>
