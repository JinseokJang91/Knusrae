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

            <div class="request-section mt-6 pt-6 border-t">
                <Button 
                    label="이 재료 정보 요청하기" 
                    icon="pi pi-send"
                    severity="secondary"
                    outlined
                    @click="showRequestDialog = true"
                />
            </div>
        </div>

        <template #footer>
            <Button label="닫기" @click="handleClose" />
        </template>
    </Dialog>

    <!-- 요청 다이얼로그 -->
    <Dialog
        v-model:visible="showRequestDialog"
        header="재료 정보 요청"
        :modal="true"
        :style="{ width: '90vw', maxWidth: '500px' }"
    >
        <div class="request-form">
            <div class="mb-4">
                <label class="block mb-2 font-semibold">재료명</label>
                <InputText v-model="requestForm.ingredientName" class="w-full" disabled />
            </div>
            
            <div class="mb-4">
                <label class="block mb-2 font-semibold">요청 유형</label>
                <Select 
                    v-model="requestForm.requestType" 
                    :options="requestTypes"
                    optionLabel="label"
                    optionValue="value"
                    class="w-full"
                />
            </div>
            
            <div class="mb-4">
                <label class="block mb-2 font-semibold">메시지 (선택사항)</label>
                <Textarea 
                    v-model="requestForm.message" 
                    rows="4"
                    class="w-full"
                    placeholder="요청 사항을 입력하세요..."
                />
            </div>
        </div>

        <template #footer>
            <Button label="취소" severity="secondary" outlined @click="showRequestDialog = false" />
            <Button label="요청하기" @click="handleRequestSubmit" :loading="requestLoading" />
        </template>
    </Dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import Dialog from 'primevue/dialog';
import Button from 'primevue/button';
import InputText from 'primevue/inputtext';
import Select from 'primevue/select';
import Textarea from 'primevue/textarea';
import ProgressSpinner from 'primevue/progressspinner';
import { useToast } from 'primevue/usetoast';
import ToastUiViewer from '@/components/editor/ToastUiViewer.vue';
import { getIngredientStorage, getIngredientPreparation, createIngredientRequest } from '@/api/ingredientApi';
import type { Ingredient, IngredientType } from '@/types/ingredient';

const props = defineProps<{
    visible: boolean;
    ingredient: Ingredient | null;
    type: IngredientType;
}>();

const emit = defineEmits<{
    (e: 'close'): void;
}>();

const toast = useToast();
const loading = ref(false);
const error = ref<string | null>(null);
const content = ref<string>('');
const summary = ref<string>('');
const showRequestDialog = ref(false);
const requestLoading = ref(false);

const requestForm = ref({
    ingredientName: '',
    requestType: 'STORAGE' as 'STORAGE' | 'PREPARATION',
    message: ''
});

const requestTypes = [
    { label: '보관법', value: 'STORAGE' },
    { label: '손질법', value: 'PREPARATION' }
];

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
    } catch (err: any) {
        console.error('재료 정보 로딩 실패:', err);
        error.value = err.message || '정보를 불러올 수 없습니다.';
    } finally {
        loading.value = false;
    }
};

const handleClose = () => {
    emit('close');
};

const handleRequestSubmit = async () => {
    if (!requestForm.value.ingredientName) {
        toast.add({
            severity: 'warn',
            summary: '알림',
            detail: '재료명을 입력해주세요.',
            life: 3000
        });
        return;
    }
    
    requestLoading.value = true;
    
    try {
        await createIngredientRequest({
            ingredientName: requestForm.value.ingredientName,
            requestType: requestForm.value.requestType,
            message: requestForm.value.message || undefined
        });
        
        toast.add({
            severity: 'success',
            summary: '요청 완료',
            detail: '재료 정보 요청이 접수되었습니다.',
            life: 3000
        });
        
        showRequestDialog.value = false;
        requestForm.value = {
            ingredientName: '',
            requestType: 'STORAGE',
            message: ''
        };
    } catch (err: any) {
        console.error('요청 생성 실패:', err);
        toast.add({
            severity: 'error',
            summary: '오류',
            detail: err.message || '요청 생성에 실패했습니다.',
            life: 3000
        });
    } finally {
        requestLoading.value = false;
    }
};

watch(() => props.visible, (newValue) => {
    if (newValue && props.ingredient) {
        requestForm.value.ingredientName = props.ingredient.name;
        requestForm.value.requestType = props.type === 'storage' ? 'STORAGE' : 'PREPARATION';
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

.request-section {
    text-align: center;
}
</style>
