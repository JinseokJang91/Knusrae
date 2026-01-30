<template>
    <div class="ingredient-register">
        <div class="page-header mb-6">
            <h1 class="text-3xl font-bold text-gray-900">재료 등록</h1>
            <p class="text-gray-600 mt-2">재료의 보관법 또는 손질법을 등록하세요</p>
        </div>

        <div class="register-form">
            <Card>
                <template #content>
                    <form @submit.prevent="handleSubmit" class="space-y-6">
                        <!-- 재료 선택 -->
                        <div class="field">
                            <label for="ingredient" class="block text-sm font-medium text-gray-700 mb-2">
                                재료 선택 <span class="text-red-500">*</span>
                            </label>
                            <AutoComplete
                                id="ingredient"
                                v-model="selectedIngredient"
                                :suggestions="ingredientSuggestions"
                                @complete="searchIngredients"
                                optionLabel="name"
                                placeholder="재료를 검색하여 선택하세요"
                                class="w-full"
                                :class="{ 'p-invalid': errors.ingredientId }"
                                @item-select="handleIngredientSelect"
                            >
                                <template #option="slotProps">
                                    <div class="flex items-center gap-2">
                                        <span>{{ slotProps.option.name }}</span>
                                        <span class="text-sm text-gray-500">({{ slotProps.option.group.name }})</span>
                                    </div>
                                </template>
                            </AutoComplete>
                            <small v-if="errors.ingredientId" class="p-error">{{ errors.ingredientId }}</small>
                        </div>

                        <!-- 등록 타입 선택 -->
                        <div class="field">
                            <label class="block text-sm font-medium text-gray-700 mb-2">
                                등록 타입 <span class="text-red-500">*</span>
                            </label>
                            <div class="flex gap-4">
                                <div class="flex items-center">
                                    <RadioButton
                                        id="storage"
                                        v-model="registerType"
                                        inputId="storage"
                                        value="storage"
                                    />
                                    <label for="storage" class="ml-2 cursor-pointer">
                                        <i class="pi pi-box mr-1"></i>
                                        재료 보관법
                                    </label>
                                </div>
                                <div class="flex items-center">
                                    <RadioButton
                                        id="preparation"
                                        v-model="registerType"
                                        inputId="preparation"
                                        value="preparation"
                                    />
                                    <label for="preparation" class="ml-2 cursor-pointer">
                                        <i class="pi pi-cut mr-1"></i>
                                        재료 손질법
                                    </label>
                                </div>
                            </div>
                            <small v-if="errors.registerType" class="p-error">{{ errors.registerType }}</small>
                        </div>

                        <!-- 요약 -->
                        <div class="field">
                            <label for="summary" class="block text-sm font-medium text-gray-700 mb-2">
                                요약 (선택)
                            </label>
                            <InputText
                                id="summary"
                                v-model="formData.summary"
                                placeholder="짧은 설명을 입력하세요 (최대 200자)"
                                class="w-full"
                                :maxlength="200"
                            />
                            <small class="text-gray-500">{{ formData.summary?.length || 0 }}/200</small>
                        </div>

                        <!-- 내용 -->
                        <div class="field">
                            <label for="content" class="block text-sm font-medium text-gray-700 mb-2">
                                내용 <span class="text-red-500">*</span>
                            </label>
                            <ToastUiEditor
                                ref="editorRef"
                                :initial-value="editorInitialValue"
                                :options="editorOptions"
                                height="400px"
                                initial-edit-type="markdown"
                                preview-style="vertical"
                                @change="onEditorChange"
                            />
                            <small v-if="errors.content" class="p-error">{{ errors.content }}</small>
                            <small v-else class="text-gray-500">{{ contentLength }}자</small>
                        </div>

                        <!-- 버튼 -->
                        <div class="flex gap-3 justify-end">
                            <Button
                                label="취소"
                                severity="secondary"
                                @click="handleCancel"
                                :disabled="submitting"
                            />
                            <Button
                                label="저장"
                                icon="pi pi-check"
                                type="submit"
                                :loading="submitting"
                                :disabled="submitting"
                            />
                        </div>
                    </form>
                </template>
            </Card>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';
import { useToast } from 'primevue/usetoast';
import Card from 'primevue/card';
import AutoComplete from 'primevue/autocomplete';
import RadioButton from 'primevue/radiobutton';
import InputText from 'primevue/inputtext';
import Button from 'primevue/button';
import ToastUiEditor from '@/components/editor/ToastUiEditor.vue';
import {
    getIngredients,
    createIngredientStorage,
    createIngredientPreparation,
    uploadContentImage
} from '@/api/ingredientApi';
import type { Ingredient } from '@/types/ingredient';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();
const toast = useToast();

const selectedIngredient = ref<Ingredient | null>(null);
const ingredientSuggestions = ref<Ingredient[]>([]);
const registerType = ref<'storage' | 'preparation'>('storage');
const submitting = ref(false);
const editorRef = ref<InstanceType<typeof ToastUiEditor> | null>(null);
const editorInitialValue = '';

const formData = ref({
    summary: ''
});

const editorOptions = {
    usageStatistics: false,
    placeholder: '상세 내용을 입력하세요 (최소 10자 이상). 이미지 삽입은 붙여넣기 또는 툴바 이미지 버튼을 사용하세요.',
    hooks: {
        addImageBlobHook: async (blob: Blob, callback: (url: string, altText?: string) => void) => {
            try {
                const file = new File([blob], 'image.png', { type: blob.type || 'image/png' });
                const { url } = await uploadContentImage(file);
                callback(url, '이미지');
            } catch (e: unknown) {
                const msg = e instanceof Error ? e.message : '이미지 업로드에 실패했습니다.';
                toast.add({
                    severity: 'error',
                    summary: '이미지 업로드 실패',
                    detail: msg,
                    life: 5000
                });
            }
        }
    }
};

const contentLength = ref(0);

function onEditorChange() {
    contentLength.value = getMarkdownFromEditor().length;
}

function getMarkdownFromEditor(): string {
    if (!editorRef.value) return '';
    return editorRef.value.getMarkdown?.() ?? '';
}

const errors = ref<{
    ingredientId?: string;
    registerType?: string;
    content?: string;
}>({});

// 재료 검색
let searchTimeout: ReturnType<typeof setTimeout> | null = null;

const searchIngredients = async (event: { query: string }) => {
    if (searchTimeout) {
        clearTimeout(searchTimeout);
    }

    searchTimeout = setTimeout(async () => {
        try {
            const query = event.query.trim();
            if (query.length < 1) {
                ingredientSuggestions.value = [];
                return;
            }

            const result = await getIngredients({
                searchQuery: query,
                limit: 20
            });
            ingredientSuggestions.value = result.ingredients;
        } catch (error) {
            console.error('재료 검색 실패:', error);
            ingredientSuggestions.value = [];
        }
    }, 300);
};

const handleIngredientSelect = (event: { value: Ingredient }) => {
    selectedIngredient.value = event.value;
};

// 폼 검증
const validateForm = (): boolean => {
    errors.value = {};
    const content = getMarkdownFromEditor().trim();

    if (!selectedIngredient.value || !selectedIngredient.value.id) {
        errors.value.ingredientId = '재료를 선택해주세요.';
    }

    if (!registerType.value) {
        errors.value.registerType = '등록 타입을 선택해주세요.';
    }

    if (!content || content.length < 10) {
        errors.value.content = '내용은 최소 10자 이상 입력해주세요.';
    }

    return Object.keys(errors.value).length === 0;
};

// 제출
const handleSubmit = async () => {
    if (!validateForm()) {
        toast.add({
            severity: 'error',
            summary: '입력 오류',
            detail: '입력한 내용을 확인해주세요.',
            life: 3000
        });
        return;
    }

    submitting.value = true;

    try {
        const content = getMarkdownFromEditor().trim();
        const request = {
            ingredientId: selectedIngredient.value!.id,
            content,
            summary: formData.value.summary?.trim() || undefined
        };

        if (registerType.value === 'storage') {
            await createIngredientStorage(request);
            toast.add({
                severity: 'success',
                summary: '등록 완료',
                detail: '재료 보관법이 등록되었습니다.',
                life: 3000
            });
        } else {
            await createIngredientPreparation(request);
            toast.add({
                severity: 'success',
                summary: '등록 완료',
                detail: '재료 손질법이 등록되었습니다.',
                life: 3000
            });
        }

        // 관리자 경로에서 왔으면 관리자페이지로, 아니면 재료 관리 화면으로
        router.push(route.path.startsWith('/admin') ? '/admin' : '/ingredient/management');
    } catch (error: any) {
        console.error('등록 실패:', error);
        toast.add({
            severity: 'error',
            summary: '등록 실패',
            detail: error.message || '등록 중 오류가 발생했습니다.',
            life: 5000
        });
    } finally {
        submitting.value = false;
    }
};

// 취소
const handleCancel = () => {
    router.push(route.path.startsWith('/admin') ? '/admin' : '/ingredient/management');
};

onMounted(() => {
    // 인증 상태 초기화 후 권한 확인
    const ensureAdmin = async () => {
        try {
            if (!authStore.isInitialized) {
                await authStore.checkAuth();
            }

            if (!authStore.isAdmin) {
                toast.add({
                    severity: 'warn',
                    summary: '접근 권한 없음',
                    detail: '재료 등록은 관리자만 이용할 수 있습니다.',
                    life: 3000
                });
                router.replace(route.path.startsWith('/admin') ? '/admin' : '/ingredient/management');
            }
        } catch (error) {
            console.error('관리자 권한 확인 실패:', error);
            router.replace(route.path.startsWith('/admin') ? '/admin' : '/ingredient/management');
        }
    };

    void ensureAdmin();
});
</script>

<style scoped>
.ingredient-register {
    padding: 24px;
    max-width: 1000px;
    margin: 0 auto;
}

.page-header {
    margin-bottom: 24px;
}

.register-form {
    margin-top: 24px;
}

.field {
    margin-bottom: 24px;
}

::deep(.p-autocomplete) {
    width: 100%;
}

::deep(.toastui-editor-defaultUI) {
    width: 100%;
}
</style>

