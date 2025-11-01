<template>
    <div class="card">
        <div class="flex items-center justify-between mb-4">
            <h2 class="text-2xl font-bold">레시피 등록</h2>
            <div class="flex gap-2">
                <button class="px-4 py-2 text-gray-600 hover:bg-gray-100 rounded-md" @click="goBack" :disabled="submitting">
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

            <!-- 썸네일 업로드 -->
            <div>
                <label class="block mb-2 font-medium">썸네일</label>
                <div class="flex items-center gap-4">
                    <input type="file" accept="image/*" @change="onThumbnailChange" :disabled="submitting" />
                    <button v-if="form.thumbnailPreview" class="px-3 py-1 text-red-600 hover:bg-red-100 rounded" @click="clearThumbnail" :disabled="submitting">
                        <span class="pi pi-times mr-1"></span>
                        제거
                    </button>
                </div>
                <div v-if="form.thumbnailPreview" class="mt-2">
                    <img :src="form.thumbnailPreview" alt="thumbnail preview" class="max-h-48 rounded border" />
                </div>
                <p class="text-sm text-gray-500 mt-1">등록 시 썸네일이 대표 이미지로 사용됩니다.</p>
            </div>

            <div>
                <label class="block mb-2 font-medium">제목</label>
                <input v-model.trim="form.title" type="text" class="px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 w-full" placeholder="레시피 제목을 입력하세요" />
            </div>

            <div>
                <label class="block mb-2 font-medium">설명</label>
                <textarea v-model.trim="form.description" class="px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 w-full" rows="4" placeholder="간단한 소개나 메모를 작성하세요"></textarea>
            </div>

            <div>
                <label class="block mb-2 font-medium">카테고리</label>
                <div v-if="categoriesError" class="mb-2 p-3 bg-red-100 border border-red-400 text-red-700 rounded">
                    {{ categoriesError }}
                </div>
                <div v-else>
                    <div v-if="categoriesLoading" class="p-3 text-gray-500 border border-dashed rounded">
                        카테고리 정보를 불러오는 중입니다...
                    </div>
                    <div v-else class="grid grid-cols-1 md:grid-cols-3 gap-4">
                        <div v-for="option in categoryOptions" :key="option.codeId" class="flex flex-col gap-2">
                            <span class="text-sm font-medium text-gray-700">{{ option.codeName }}</span>
                            <select v-model="form.categories[option.codeId]" class="px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500">
                                <option value="">선택하세요</option>
                                <option v-for="detail in option.details" :key="detail.detailCodeId" :value="detail.detailCodeId">
                                    {{ detail.codeName }}
                                </option>
                            </select>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 단계 관리 -->
            <div>
                <div class="flex items-center justify-between mb-2">
                    <label class="font-medium">조리 순서</label>
                    <button class="px-4 py-2 bg-blue-500 text-white rounded-md hover:bg-blue-600 disabled:opacity-50" @click="addStep" :disabled="submitting">
                        <span class="pi pi-plus mr-2"></span>
                        <span>단계 추가</span>
                    </button>
                </div>
                <div v-if="form.steps.length === 0" class="p-3 text-gray-500 border rounded">아직 단계가 없습니다. '단계 추가'를 눌러 시작하세요.</div>

                <div v-for="(step, index) in form.steps" :key="step.id" class="border rounded p-3 mb-3 bg-gray-50">
                    <div class="flex items-center justify-between mb-3">
                        <div class="font-medium">단계 {{ index + 1 }}</div>
                        <div class="flex gap-2">
                            <button class="px-2 py-1 text-blue-600 hover:bg-blue-100 rounded" @click="moveStepUp(index)" :disabled="index === 0 || submitting">
                                <span class="pi pi-arrow-up"></span>
                            </button>
                            <button class="px-2 py-1 text-blue-600 hover:bg-blue-100 rounded" @click="moveStepDown(index)" :disabled="index === form.steps.length - 1 || submitting">
                                <span class="pi pi-arrow-down"></span>
                            </button>
                            <button class="px-2 py-1 text-red-600 hover:bg-red-100 rounded" @click="removeStep(index)" :disabled="submitting">
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
                            <textarea v-model.trim="step.text" class="px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 w-full" rows="6" placeholder="이 단계에서의 설명을 작성하세요"></textarea>
                        </div>
                    </div>
                </div>
            </div>

            <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div>
                    <label class="block mb-2 font-medium">공개 여부</label>
                    <select v-model="form.visibility" class="px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 w-full">
                        <option value="PUBLIC">공개</option>
                        <option value="PRIVATE">비공개</option>
                    </select>
                </div>
                <div>
                    <label class="block mb-2 font-medium">상태</label>
                    <select v-model="form.status" class="px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 w-full">
                        <option value="DRAFT">초안</option>
                        <option value="PUBLISHED">발행</option>
                    </select>
                </div>
            </div>

            <div class="flex justify-end gap-2 mt-2">
                <button class="px-4 py-2 text-gray-600 hover:bg-gray-100 rounded-md" @click="saveAsDraft" :disabled="submitting">
                    <span class="pi pi-save mr-2"></span>
                    <span>초안 저장</span>
                </button>
                <button class="px-4 py-2 bg-green-500 text-white rounded-md hover:bg-green-600 disabled:opacity-50" @click="submit" :disabled="submitting || !isValid">
                    <span class="pi pi-check mr-2"></span>
                    <span>등록</span>
                </button>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { httpForm, httpJson } from '@/utils/http';
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();

interface RecipeStepDraft {
    id: string;
    file?: File | null;
    previewUrl?: string;
    text: string;
}

interface CommonCodeDetailOption {
    detailCodeId: string;
    codeName: string;
}

interface CommonCodeOption {
    codeId: string;
    codeName: string;
    details: CommonCodeDetailOption[];
}

interface RecipeDraft {
    title: string;
    description: string;
    status: 'DRAFT' | 'PUBLISHED';
    visibility: 'PUBLIC' | 'PRIVATE';
    memberId: number;
    thumbnailFile?: File | null;
    thumbnailPreview?: string;
    steps: RecipeStepDraft[];
    categories: Record<string, string>;
}

const submitting = ref(false);
const error = ref<string | null>(null);
const categoriesLoading = ref(false);
const categoriesError = ref<string | null>(null);
const categoryOptions = ref<CommonCodeOption[]>([]);

const form = reactive<RecipeDraft>({
    title: '',
    description: '',
    status: 'DRAFT',
    visibility: 'PUBLIC',
    memberId: 1,
    thumbnailFile: null,
    thumbnailPreview: '',
    steps: [],
    categories: {}
});

const isValid = computed(() => {
    const basicValid = Boolean(form.title.trim());
    const stepsValid = form.steps.length > 0 && form.steps.every((s) => s.text.trim());
    const categoriesValid = categoryOptions.value.length === 0 || categoryOptions.value.every((option) => !!form.categories[option.codeId]);
    return basicValid && stepsValid && categoriesValid;
});

onMounted(() => {
    loadCategoryOptions();
});

async function loadCategoryOptions() {
    categoriesLoading.value = true;
    categoriesError.value = null;
    try {
        const response = await httpJson(import.meta.env.VITE_API_BASE_URL_COOK, '/api/common-codes?codeGroup=CATEGORY', {
            method: 'GET',
            attachAuth: false
        });

        if (Array.isArray(response)) {
            categoryOptions.value = response;
            categoryOptions.value.forEach((option) => {
                if (form.categories[option.codeId] === undefined) {
                    form.categories[option.codeId] = '';
                }
            });
        } else {
            categoryOptions.value = [];
        }
    } catch (e) {
        console.error('카테고리 정보를 불러오지 못했습니다.', e);
        categoriesError.value = '카테고리 정보를 불러오지 못했습니다.';
    } finally {
        categoriesLoading.value = false;
    }
}

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

function onThumbnailChange(e: Event) {
    const input = e.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    form.thumbnailFile = file;
    if (form.thumbnailPreview) URL.revokeObjectURL(form.thumbnailPreview);
    form.thumbnailPreview = URL.createObjectURL(file);
}

function clearThumbnail() {
    if (form.thumbnailPreview) URL.revokeObjectURL(form.thumbnailPreview);
    form.thumbnailPreview = '';
    form.thumbnailFile = null;
}

function goBack() {
    router.push('/my/recipes');
}

function buildRecipePayload(statusOverride?: 'DRAFT' | 'PUBLISHED') {
    const categories = categoryOptions.value
        .map((option) => ({
            codeId: option.codeId,
            detailCodeId: form.categories[option.codeId]
        }))
        .filter((category) => Boolean(category.detailCodeId));

    return {
        title: form.title,
        description: form.description,
        status: statusOverride ?? form.status,
        visibility: form.visibility,
        memberId: form.memberId,
        categories,
        steps: form.steps.map((s, idx) => ({ order: idx + 1, text: s.text.trim() }))
    };
}

async function saveAsDraft() {
    submitting.value = true;
    error.value = null;
    try {
        const payload = buildRecipePayload('DRAFT');
        await httpJson(import.meta.env.VITE_API_BASE_URL_COOK, '/api/recipe/draft', {
            method: 'POST',
            body: JSON.stringify(payload)
        });

        alert('초안이 저장되었습니다.');
    } catch (e) {
        console.error(e);
        error.value = '초안 저장 중 오류가 발생했습니다.';
    } finally {
        submitting.value = false;
    }
}

async function submit() {
    if (!isValid.value) {
        error.value = '필수 항목을 모두 입력해주세요.';
        return;
    }
    submitting.value = true;
    error.value = null;
    try {
        // 토큰 검증
        const token = localStorage.getItem('accessToken');

        if (!token) {
            throw new Error('로그인이 필요합니다. 다시 로그인해주세요.');
        }

        // 멀티파트 폼 구성 (이미지 + 텍스트 단계)
        const formData = new FormData();

        const recipePayload = buildRecipePayload();

        // Blob을 사용하여 명시적으로 MIME 타입 설정
        const recipeBlob = new Blob([JSON.stringify(recipePayload)], {
            type: 'application/json; charset=utf-8'
        });
        formData.append('recipe', recipeBlob, 'recipe.json');

        // 썸네일이 있다면 먼저 추가 (대표 이미지가 됨)
        if (form.thumbnailFile) {
            formData.append('images', form.thumbnailFile, 'thumbnail.png');
        }

        form.steps.forEach((s, idx) => {
            if (s.file) formData.append('images', s.file, `step-${idx + 1}.png`);
        });

        // 대표 이미지 인덱스 설정: 썸네일이 있으면 0, 없으면 첫 번째 이미지(0)
        formData.append('mainImageIndex', '0');

        // 실제 API 엔드포인트로 전송 (토큰 자동 첨부)
        await httpForm(import.meta.env.VITE_API_BASE_URL_COOK, '/api/recipe', formData, { method: 'POST' });

        alert('등록이 완료되었습니다.');
        router.push('/my/recipes');
    } catch (e) {
        console.error(e);
        error.value = e instanceof Error ? e.message : '레시피 등록 중 오류가 발생했습니다.';
    } finally {
        submitting.value = false;
    }
}
</script>

<style scoped></style>
