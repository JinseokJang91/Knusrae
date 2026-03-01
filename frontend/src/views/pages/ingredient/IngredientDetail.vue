<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import Button from 'primevue/button';
import PageStateBlock from '@/components/common/PageStateBlock.vue';
import ToastUiViewer from '@/components/editor/ToastUiViewer.vue';
import { getIngredientStorage, getIngredientPreparation } from '@/api/ingredientApi';
import type { IngredientType } from '@/types/ingredient';

const route = useRoute();
const router = useRouter();

const loading = ref(false);
const error = ref<string | null>(null);
/** 404/NOT_FOUND 등 상세 데이터가 없을 때 true → '준비 중' 안내 표시 */
const isNotFoundError = ref(false);
const content = ref('');
const summary = ref('');
const ingredientName = ref('');

const detailType = computed<IngredientType>(() => {
    const t = route.query.type as string;
    return t === 'preparation' ? 'preparation' : 'storage';
});

const pageTitle = computed(() => {
    const typeLabel = detailType.value === 'storage' ? '보관법' : '손질법';
    return ingredientName.value ? `${ingredientName.value} ${typeLabel}` : typeLabel;
});

/** 에러 시 사용자에게 보여줄 제목 (404는 '준비 중' 안내) */
const errorDisplayTitle = computed(() => (isNotFoundError.value ? '준비 중인 콘텐츠입니다' : '정보를 불러올 수 없습니다'));
/** 에러 시 사용자에게 보여줄 메시지 (404는 안내 문구, 그 외는 원본 메시지) */
const errorDisplayMessage = computed(() => (isNotFoundError.value ? '해당 재료 정보는 아직 준비 중입니다. 조금만 기다려주세요 😉 ' : (error.value ?? '')));

const ingredientId = computed(() => {
    const id = route.params.id;
    return id ? Number(id) : 0;
});

const loadContent = async () => {
    const id = ingredientId.value;
    if (!id || id <= 0) {
        error.value = '잘못된 재료 정보입니다.';
        return;
    }

    loading.value = true;
    error.value = null;
    isNotFoundError.value = false;

    try {
        if (detailType.value === 'storage') {
            const data = await getIngredientStorage(id);
            content.value = data.content;
            summary.value = data.summary;
            ingredientName.value = data.ingredient?.name ?? '';
        } else {
            const data = await getIngredientPreparation(id);
            content.value = data.content;
            summary.value = data.summary;
            ingredientName.value = data.ingredient?.name ?? '';
        }
    } catch (err: unknown) {
        console.error('재료 정보 로딩 실패:', err);
        const message = err instanceof Error ? err.message : '정보를 불러올 수 없습니다.';
        const isNotFound = message.includes('NOT_FOUND') || message.includes('404') || message.includes('찾을 수 없습니다');
        isNotFoundError.value = isNotFound;
        error.value = message;
    } finally {
        loading.value = false;
    }
};

const goBack = () => {
    router.push({
        path: '/ingredient/management',
        query: { type: detailType.value }
    });
};

watch(
    [() => route.params.id, () => route.query.type],
    () => {
        content.value = '';
        summary.value = '';
        ingredientName.value = '';
        loadContent();
    },
    { immediate: true }
);
</script>

<template>
    <div class="page-container page-container--card page-container--wide">
        <div class="ingredient-detail-page">
            <header class="detail-header">
                <Button icon="pi pi-arrow-left" label="목록으로" text class="back-btn" aria-label="재료 관리 목록으로" @click="goBack" />
                <h1 v-if="!loading && !error" class="detail-title">
                    {{ pageTitle }}
                </h1>
            </header>

            <PageStateBlock v-if="loading" state="loading" loading-message="정보를 불러오는 중..." />
            <PageStateBlock v-else-if="error" state="error" :error-title="errorDisplayTitle" :error-message="errorDisplayMessage" retry-label="목록으로" @retry="goBack" />

            <div v-else-if="content" class="ingredient-detail-body">
                <section v-if="summary" class="summary-section">
                    <h2 class="summary-title">요약</h2>
                    <p class="summary-text">{{ summary }}</p>
                </section>

                <section class="content-section">
                    <ToastUiViewer :key="content" :initial-value="content" />
                </section>
            </div>
        </div>
    </div>
</template>

<style scoped lang="scss">
.ingredient-detail-page {
    margin: 0;
    padding: 0 0 2rem;
}

.detail-header {
    display: flex;
    align-items: center;
    gap: 1rem;
    flex-wrap: wrap;
    margin-bottom: 1.75rem;
    padding-bottom: 1rem;
    border-bottom: 1px solid var(--surface-border);
}

.back-btn {
    color: var(--text-color-secondary);
    flex-shrink: 0;

    &:hover {
        color: var(--primary-color);
    }
}

.detail-title {
    font-size: 1.5rem;
    font-weight: 700;
    color: var(--text-color);
    margin: 0;
    line-height: 1.3;
}

.summary-section {
    margin-bottom: 1.75rem;
    padding: 1.25rem 1.5rem;
    background: var(--surface-100);
    border-radius: 12px;
    border-left: 4px solid var(--primary-color);
}

.summary-title {
    font-size: 0.95rem;
    font-weight: 600;
    color: var(--text-color);
    margin: 0 0 0.5rem 0;
}

.summary-text {
    font-size: 1rem;
    color: var(--text-color-secondary);
    margin: 0;
    line-height: 1.65;
}

.content-section {
    line-height: 1.8;
}

.content-section :deep(.toastui-editor-contents) {
    font-size: 1rem;
}

@media (max-width: 768px) {
    .ingredient-detail-page {
        padding: 0 0 1.5rem;
    }

    .detail-header {
        margin-bottom: 1.25rem;
        padding-bottom: 0.75rem;
    }

    .detail-title {
        font-size: 1.25rem;
    }

    .summary-section {
        padding: 1rem 1.25rem;
        margin-bottom: 1.25rem;
    }
}
</style>
