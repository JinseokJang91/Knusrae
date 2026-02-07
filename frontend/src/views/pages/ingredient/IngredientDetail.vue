<template>
    <div class="ingredient-detail-page">
        <div class="detail-header">
            <Button
                icon="pi pi-arrow-left"
                label="목록으로"
                text
                class="p-button-text back-btn"
                @click="goBack"
            />
            <h1 v-if="!loading && !error" class="detail-title">
                {{ pageTitle }}
            </h1>
        </div>

        <div v-if="loading" class="text-center py-12">
            <ProgressSpinner />
            <p class="text-gray-600 mt-3">정보를 불러오는 중...</p>
        </div>

        <div v-else-if="error" class="text-center py-12">
            <i class="pi pi-exclamation-triangle text-6xl text-red-500 mb-4"></i>
            <h2 class="text-xl font-semibold text-gray-600 mb-2">정보를 불러올 수 없습니다</h2>
            <p class="text-gray-600 mb-4">{{ error }}</p>
            <Button label="목록으로" @click="goBack" />
        </div>

        <div v-else-if="content" class="ingredient-detail-body">
            <div v-if="summary" class="summary-section">
                <h3 class="summary-title">요약</h3>
                <p class="summary-text">{{ summary }}</p>
            </div>

            <div class="content-section">
                <ToastUiViewer :key="content" :initial-value="content" />
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import Button from 'primevue/button';
import ProgressSpinner from 'primevue/progressspinner';
import ToastUiViewer from '@/components/editor/ToastUiViewer.vue';
import { getIngredientStorage, getIngredientPreparation } from '@/api/ingredientApi';
import type { IngredientType } from '@/types/ingredient';

const route = useRoute();
const router = useRouter();

const loading = ref(false);
const error = ref<string | null>(null);
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
        error.value = err instanceof Error ? err.message : '정보를 불러올 수 없습니다.';
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

watch([() => route.params.id, () => route.query.type], () => {
    content.value = '';
    summary.value = '';
    ingredientName.value = '';
    loadContent();
}, { immediate: true });
</script>

<style scoped>
.ingredient-detail-page {
    max-width: 800px;
    margin: 0 auto;
    padding: 0 1rem 2rem;
}

.detail-header {
    margin-bottom: 1.5rem;
}

.back-btn {
    margin-bottom: 0.5rem;
    color: var(--text-color-secondary);
}

.back-btn:hover {
    color: var(--primary-color);
}

.detail-title {
    font-size: 1.5rem;
    font-weight: 700;
    color: var(--text-color);
    margin: 0;
}

.summary-section {
    margin-bottom: 1.5rem;
    padding: 1rem 1.25rem;
    background: var(--blue-50, #eff6ff);
    border-radius: 12px;
    border-left: 4px solid var(--primary-color);
}

.summary-title {
    font-size: 0.95rem;
    font-weight: 600;
    color: var(--blue-900, #1e3a8a);
    margin: 0 0 0.5rem 0;
}

.summary-text {
    font-size: 1rem;
    color: var(--blue-800, #1e40af);
    margin: 0;
    line-height: 1.6;
}

.content-section {
    line-height: 1.8;
}

.content-section :deep(.toastui-editor-contents) {
    font-size: 1rem;
}

@media (max-width: 768px) {
    .ingredient-detail-page {
        padding: 0 0.75rem 1.5rem;
    }

    .detail-title {
        font-size: 1.25rem;
    }
}
</style>
