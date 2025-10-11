<template>
    <div class="card">
        <div class="flex items-center justify-between mb-4">
            <h2 class="text-2xl font-bold">레시피 등록</h2>
            <div class="flex gap-2">
                <button class="p-button p-component p-button-text" @click="goBack" :disabled="submitting">
                    <span class="pi pi-arrow-left mr-2"></span>
                    <span>목록으로</span>
                </button>
            </div>
        </div>

        <!-- 에러 메시지 표시 -->
        <div v-if="error" class="mb-4 p-3 bg-red-100 border border-red-400 text-red-700 rounded">
            {{ error }}
        </div>

        <div class="grid grid-cols-1 gap-4">
            <div>
                <label class="block mb-2 font-medium">제목</label>
                <input v-model.trim="form.title" type="text" class="p-inputtext p-component w-full" placeholder="레시피 제목을 입력하세요" />
            </div>

            <div>
                <label class="block mb-2 font-medium">설명</label>
                <textarea v-model.trim="form.description" class="p-inputtextarea p-component w-full" rows="4" placeholder="간단한 소개나 메모를 작성하세요"></textarea>
            </div>

            <!-- 단계 관리 -->
            <div>
                <div class="flex items-center justify-between mb-2">
                    <label class="font-medium">조리 순서</label>
                    <button class="p-button p-component" @click="addStep" :disabled="submitting">
                        <span class="pi pi-plus mr-2"></span>
                        <span>단계 추가</span>
                    </button>
                </div>
                <div v-if="form.steps.length === 0" class="p-3 text-surface-500 border rounded">아직 단계가 없습니다. '단계 추가'를 눌러 시작하세요.</div>

                <div v-for="(step, index) in form.steps" :key="step.id" class="border rounded p-3 mb-3 bg-surface-50">
                    <div class="flex items-center justify-between mb-3">
                        <div class="font-medium">단계 {{ index + 1 }}</div>
                        <div class="flex gap-2">
                            <button class="p-button p-component p-button-text" @click="moveStepUp(index)" :disabled="index === 0 || submitting">
                                <span class="pi pi-arrow-up"></span>
                            </button>
                            <button class="p-button p-component p-button-text" @click="moveStepDown(index)" :disabled="index === form.steps.length - 1 || submitting">
                                <span class="pi pi-arrow-down"></span>
                            </button>
                            <button class="p-button p-component p-button-text text-red-600" @click="removeStep(index)" :disabled="submitting">
                                <span class="pi pi-trash"></span>
                            </button>
                        </div>
                    </div>

                    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <div>
                            <label class="block mb-2">이미지</label>
                            <input type="file" accept="image/*" @change="onStepImageChange($event, step)" :disabled="submitting" />
                            <div v-if="step.previewUrl" class="mt-2">
                                <img :src="step.previewUrl" alt="step preview" class="max-h-48 rounded border" />
                            </div>
                        </div>
                        <div>
                            <label class="block mb-2">설명</label>
                            <textarea v-model.trim="step.text" class="p-inputtextarea p-component w-full" rows="6" placeholder="이 단계에서의 설명을 작성하세요"></textarea>
                        </div>
                    </div>
                </div>
            </div>

            <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div>
                    <label class="block mb-2 font-medium">공개 여부</label>
                    <select v-model="form.visibility" class="p-inputtext p-component w-full">
                        <option value="PUBLIC">공개</option>
                        <option value="PRIVATE">비공개</option>
                    </select>
                </div>
                <div>
                    <label class="block mb-2 font-medium">상태</label>
                    <select v-model="form.status" class="p-inputtext p-component w-full">
                        <option value="DRAFT">초안</option>
                        <option value="PUBLISHED">발행</option>
                    </select>
                </div>
            </div>

            <div class="flex justify-end gap-2 mt-2">
                <button class="p-button p-component p-button-text" @click="saveAsDraft" :disabled="submitting">
                    <span class="pi pi-save mr-2"></span>
                    <span>초안 저장</span>
                </button>
                <button class="p-button p-component" @click="submit" :disabled="submitting || !isValid">
                    <span class="pi pi-check mr-2"></span>
                    <span>등록</span>
                </button>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { httpForm, httpJson } from '@/utils/http';
import { computed, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();

interface RecipeStepDraft {
    id: string;
    file?: File | null;
    previewUrl?: string;
    text: string;
}

interface RecipeDraft {
    title: string;
    description: string;
    category: string;
    status: 'DRAFT' | 'PUBLISHED';
    visibility: 'PUBLIC' | 'PRIVATE';
    memberId: number;
    steps: RecipeStepDraft[];
}

const submitting = ref(false);
const error = ref<string | null>(null);

const form = reactive<RecipeDraft>({
    title: '',
    description: '',
    category: 'TEST',
    status: 'DRAFT',
    visibility: 'PUBLIC',
    memberId: 1,
    steps: []
});

const isValid = computed(() => {
    return Boolean(form.title.trim()) && form.steps.length > 0 && form.steps.every((s) => s.text.trim());
});

function addStep() {
    form.steps.push({ id: crypto.randomUUID(), file: null, text: '', previewUrl: '' });
}

function removeStep(index: number) {
    const [removed] = form.steps.splice(index, 1);
    if (removed && removed.previewUrl) URL.revokeObjectURL(removed.previewUrl);
}

function moveStepUp(index: number) {
    if (index <= 0) return;
    const tmp = form.steps[index - 1];
    form.steps[index - 1] = form.steps[index];
    form.steps[index] = tmp;
}

function moveStepDown(index: number) {
    if (index >= form.steps.length - 1) return;
    const tmp = form.steps[index + 1];
    form.steps[index + 1] = form.steps[index];
    form.steps[index] = tmp;
}

function onStepImageChange(e: Event, step: RecipeStepDraft) {
    const input = e.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    step.file = file;
    if (step.previewUrl) URL.revokeObjectURL(step.previewUrl);
    step.previewUrl = URL.createObjectURL(file);
}

function goBack() {
    router.push('/my/recipes');
}

async function saveAsDraft() {
    submitting.value = true;
    error.value = null;
    try {
        await httpJson(import.meta.env.VITE_API_BASE_URL_COOK, '/api/recipe/draft', {
            method: 'POST',
            body: JSON.stringify(form)
        });

        alert('초안이 저장되었습니다.');
    } catch (e) {
        error.value = '초안 저장 중 오류가 발생했습니다.';
    } finally {
        submitting.value = false;
    }
}

async function submit() {
    if (!isValid.value) return;
    submitting.value = true;
    error.value = null;
    try {
        // 토큰 검증
        const token = localStorage.getItem('accessToken');
        console.log('🔐 RecipeCreate - Current Token:', token ? '토큰 존재' : '토큰 없음');

        if (!token) {
            throw new Error('로그인이 필요합니다. 다시 로그인해주세요.');
        }

        // 멀티파트 폼 구성 (이미지 + 텍스트 단계)
        const formData = new FormData();

        const recipePayload = {
            title: form.title,
            description: form.description,
            category: 'food', // TODO: 카테고리 선택 후 변경
            status: form.status,
            visibility: form.visibility,
            memberId: 1, // TODO: 로그인 후 변경
            steps: form.steps.map((s, idx) => ({ order: idx + 1, text: s.text }))
            // mainImageIndex: '0'
        };
        console.log('recipePayload : ', recipePayload);

        // Blob을 사용하여 명시적으로 MIME 타입 설정
        const recipeBlob = new Blob([JSON.stringify(recipePayload)], { 
            type: 'application/json; charset=utf-8' 
        });
        formData.append('recipe', recipeBlob, 'recipe.json');
        console.log('formData type 1 : ', (formData.get('recipe') as File).type);
        console.log('formData type 2 : ', (formData.get('recipe') as Blob).type);

        // formData.append('title', form.title);
        // formData.append('description', form.description);
        // formData.append('status', form.status);
        // formData.append('visibility', form.visibility);

        // const stepsPayload = form.steps.map((s, idx) => ({ order: idx + 1, text: s.text }));
        // formData.append('steps', new Blob([JSON.stringify(stepsPayload)], { type: 'application/json' }));

        form.steps.forEach((s, idx) => {
            console.log('s : ', s);
            if (s.file) formData.append(`images`, s.file, `step-${idx + 1}.png`);
        });

        formData.append('mainImageIndex', '0');

        // API 호출 전 로깅
        console.log('🚀 RecipeCreate - API 호출 시작:', {
            baseUrl: import.meta.env.VITE_API_BASE_URL_COOK,
            endpoint: '/api/recipe',
            method: 'POST',
            formDataKeys: Array.from(formData.keys())
        });

        // 실제 API 엔드포인트로 전송 (토큰 자동 첨부)
        await httpForm(import.meta.env.VITE_API_BASE_URL_COOK, '/api/recipe', formData, { method: 'POST' });

        alert('등록이 완료되었습니다.');
        router.push('/my/recipes');
    } catch (e) {
        error.value = '레시피 등록 중 오류가 발생했습니다.';
    } finally {
        submitting.value = false;
    }
}
</script>

<style scoped></style>
