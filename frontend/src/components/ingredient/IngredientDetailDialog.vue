<template>
    <Dialog 
        :visible="visible" 
        :header="dialogTitle"
        :style="{ width: '90vw', maxWidth: '800px' }"
        :modal="true"
        @hide="handleClose"
        :closable="true"
    >
        <div v-if="loading" class="text-center py-8">
            <ProgressSpinner />
            <p class="text-gray-600 mt-3">정보를 불러오는 중...</p>
        </div>

        <div v-else-if="error" class="text-center py-8">
            <i class="pi pi-exclamation-triangle text-6xl text-red-500 mb-4"></i>
            <h3 class="text-xl font-semibold text-gray-600 mb-2">정보를 불러올 수 없습니다</h3>
            <p class="text-gray-600 mb-4">{{ error }}</p>
            <Button label="닫기" @click="handleClose" />
        </div>

        <div v-else-if="content" class="ingredient-detail">
            <div v-if="summary" class="summary-section mb-4 p-4 bg-blue-50 rounded-lg">
                <h4 class="font-semibold text-blue-900 mb-2">요약</h4>
                <p class="text-blue-800">{{ summary }}</p>
            </div>

            <div class="content-section">
                <ToastUiViewer :key="content" :initial-value="content" />
            </div>
        </div>

        <template #footer>
            <Button label="닫기" @click="handleClose" />
        </template>
    </Dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import Dialog from 'primevue/dialog';
import Button from 'primevue/button';
import ProgressSpinner from 'primevue/progressspinner';
import ToastUiViewer from '@/components/editor/ToastUiViewer.vue';
import { getIngredientStorage, getIngredientPreparation } from '@/api/ingredientApi';
import type { Ingredient, IngredientType } from '@/types/ingredient';

const props = defineProps<{
    visible: boolean;
    ingredient: Ingredient | null;
    type: IngredientType;
}>();

const emit = defineEmits<{
    (e: 'close'): void;
}>();

const loading = ref(false);
const error = ref<string | null>(null);
const content = ref<string>('');
const summary = ref<string>('');

const dialogTitle = computed(() => {
    if (!props.ingredient) return '';
    const typeLabel = props.type === 'storage' ? '보관법' : '손질법';
    return `${props.ingredient.name} ${typeLabel}`;
});

const loadContent = async () => {
    if (!props.ingredient) return;
    
    loading.value = true;
    error.value = null;
    
    try {
        if (props.type === 'storage') {
            const data = await getIngredientStorage(props.ingredient.id);
            content.value = data.content;
            summary.value = data.summary;
        } else {
            const data = await getIngredientPreparation(props.ingredient.id);
            content.value = data.content;
            summary.value = data.summary;
        }
    } catch (err: unknown) {
        console.error('재료 정보 로딩 실패:', err);
        error.value = err instanceof Error ? err.message : '정보를 불러올 수 없습니다.';
    } finally {
        loading.value = false;
    }
};

const handleClose = () => {
    emit('close');
};

watch(() => props.visible, (newValue) => {
    if (newValue && props.ingredient) {
        loadContent();
    } else {
        content.value = '';
        summary.value = '';
        error.value = null;
    }
});
</script>

<style scoped>
.ingredient-detail {
    max-height: 70vh;
    overflow-y: auto;
}

.summary-section {
    border-left: 4px solid var(--primary-color);
}

.content-section {
    line-height: 1.8;
}

.content-section :deep(.toastui-editor-contents) {
    font-size: 1rem;
}
</style>
