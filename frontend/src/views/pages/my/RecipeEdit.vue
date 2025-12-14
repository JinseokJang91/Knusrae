<template>
    <div class="card">
        <div class="flex items-center justify-between mb-4">
            <h2 class="text-2xl font-bold">레시피 수정</h2>
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

        <!-- 로딩 상태 -->
        <div v-if="initialLoading" class="flex justify-center items-center py-8">
            <div class="pi pi-spinner pi-spin mr-2"></div>
            <span>레시피 정보를 불러오는 중...</span>
        </div>

        <div v-else class="grid grid-cols-1 gap-4">

            <!-- 썸네일 및 기본 정보 -->
            <div class="grid grid-cols-1 md:grid-cols-5 gap-4">
                <!-- 썸네일 업로드 (좌측) -->
                <div class="md:col-span-2">
                    <label class="block mb-2 font-medium"><b>대표 사진</b></label>
                    <input ref="thumbnailInputRef" type="file" accept="image/*" @change="onThumbnailChange" :disabled="submitting" class="hidden" />
                    <div 
                        class="relative w-full aspect-[5/3] bg-gray-200 border-2 border-dashed border-gray-300 rounded-md cursor-pointer hover:bg-gray-300 hover:border-gray-400 transition-colors flex items-center justify-center"
                        @click="() => !submitting && thumbnailInputRef?.click()"
                    >
                        <div v-if="!form.thumbnailPreview" class="text-center text-gray-500">
                            <span class="pi pi-image text-4xl block mb-2"></span>
                            <span class="text-sm">이미지를 클릭하여 추가하세요</span>
                        </div>
                        <div v-else class="relative w-full h-full">
                            <img :src="form.thumbnailPreview" alt="thumbnail preview" class="w-full h-full object-cover rounded-md" />
                            <button 
                                class="absolute top-2 right-2 w-8 h-8 bg-red-500 text-white rounded-full flex items-center justify-center hover:bg-red-600 transition-colors shadow-lg"
                                @click.stop="clearThumbnail"
                                :disabled="submitting"
                            >
                                <span class="pi pi-times"></span>
                            </button>
                        </div>
                    </div>
                    <p class="text-sm text-gray-500 mt-1">등록 시 썸네일이 대표 이미지로 사용됩니다.</p>
                </div>

                <!-- 제목 및 소개 (우측) -->
                <div class="md:col-span-3 flex flex-col gap-4">
                    <div>
                        <label class="block mb-2 font-medium"><b>제목</b></label>
                        <input v-model.trim="form.title" type="text" class="px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 w-full" placeholder="레시피 제목을 입력하세요" />
                    </div>
                    <div class="flex-1">
                        <label class="block mb-2 font-medium"><b>소개</b></label>
                        <textarea v-model.trim="form.description" class="px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 w-full h-full min-h-[120px]" placeholder="간단한 소개나 메모를 작성하세요"></textarea>
                    </div>
                </div>
            </div>

            <div>
                <label class="block mb-2 font-medium"><b>카테고리</b></label>
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

            <div>
                <label class="block mb-2 font-medium"><b>요리팁</b></label>
                <div v-if="cookingTipsError" class="mb-2 p-3 bg-red-100 border border-red-400 text-red-700 rounded">
                    {{ cookingTipsError }}
                </div>
                <div v-else>
                    <div v-if="cookingTipsLoading" class="p-3 text-gray-500 border border-dashed rounded">
                        요리팁 정보를 불러오는 중입니다...
                    </div>
                    <div v-else class="grid grid-cols-1 md:grid-cols-3 gap-4">
                        <div v-for="option in cookingTipsOptions" :key="option.codeId" class="flex flex-col gap-2">
                            <span class="text-sm font-medium text-gray-700">{{ option.codeName }}</span>
                            <select v-model="form.cookingTips[option.codeId]" class="px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500">
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
                    <label class="font-medium"><b>조리 순서</b></label>
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

                    <div class="grid grid-cols-1 md:grid-cols-5 gap-4">
                        <div class="md:col-span-2">
                            <label class="block mb-2">이미지</label>
                            <input 
                                :ref="el => { if (el) stepInputRefs[step.id] = el as HTMLInputElement }" 
                                type="file" 
                                accept="image/*" 
                                @change="onStepImageChange($event, step)" 
                                :disabled="submitting" 
                                class="hidden" 
                            />
                            <div 
                                class="relative w-full aspect-[5/3] bg-gray-200 border-2 border-dashed border-gray-300 rounded-md cursor-pointer hover:bg-gray-300 hover:border-gray-400 transition-colors flex items-center justify-center"
                                @click="() => !submitting && stepInputRefs[step.id]?.click()"
                            >
                                <div v-if="!step.previewUrl" class="text-center text-gray-500">
                                    <span class="pi pi-image text-4xl block mb-2"></span>
                                    <span class="text-sm">이미지를 클릭하여 추가하세요</span>
                                </div>
                                <div v-else class="relative w-full h-full">
                                    <img :src="step.previewUrl" alt="step preview" class="w-full h-full object-cover rounded-md" />
                                    <button 
                                        class="absolute top-2 right-2 w-8 h-8 bg-red-500 text-white rounded-full flex items-center justify-center hover:bg-red-600 transition-colors shadow-lg"
                                        @click.stop="clearStepImage(step)"
                                        :disabled="submitting"
                                    >
                                        <span class="pi pi-times"></span>
                                    </button>
                                </div>
                            </div>
                        </div>
                        <div class="md:col-span-3">
                            <label class="block mb-2">설명</label>
                            <textarea v-model.trim="step.text" class="px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 w-full" rows="6" placeholder="이 단계에서의 설명을 작성하세요"></textarea>
                        </div>
                    </div>
                </div>
            </div>

            <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div>
                    <label class="block mb-2 font-medium"><b>공개 여부</b></label>
                    <select v-model="form.visibility" class="px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 w-full">
                        <option value="PUBLIC">공개</option>
                        <option value="PRIVATE">비공개</option>
                    </select>
                </div>
                <div>
                    <label class="block mb-2 font-medium"><b>상태</b></label>
                    <select v-model="form.status" class="px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 w-full">
                        <option value="DRAFT">초안</option>
                        <option value="PUBLISHED">발행</option>
                    </select>
                </div>
            </div>

            <div class="flex justify-end gap-2 mt-2">
                <button class="px-4 py-2 text-gray-600 hover:bg-gray-100 rounded-md" @click="goBack" :disabled="submitting">
                    <span class="pi pi-times mr-2"></span>
                    <span>취소</span>
                </button>
                <button class="px-4 py-2 bg-green-500 text-white rounded-md hover:bg-green-600 disabled:opacity-50" @click="submit" :disabled="submitting || !isValid">
                    <span class="pi pi-check mr-2"></span>
                    <span>수정</span>
                </button>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { httpForm, httpJson } from '@/utils/http';
import { computed, onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const router = useRouter();
const route = useRoute();
const recipeId = computed(() => Number(route.params.id));

interface RecipeStepDraft {
    id: string;
    file?: File | null;
    previewUrl?: string;
    text: string;
    existingImageUrl?: string; // 기존 이미지 URL
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
    cookingTips: Record<string, string>;
}

const initialLoading = ref(true);
const submitting = ref(false);
const error = ref<string | null>(null);
const categoriesLoading = ref(false);
const categoriesError = ref<string | null>(null);
const categoryOptions = ref<CommonCodeOption[]>([]);
const cookingTipsLoading = ref(false);
const cookingTipsError = ref<string | null>(null);
const cookingTipsOptions = ref<CommonCodeOption[]>([]);
const thumbnailInputRef = ref<HTMLInputElement | null>(null);
const stepInputRefs = ref<Record<string, HTMLInputElement>>({});

const form = reactive<RecipeDraft>({
    title: '',
    description: '',
    status: 'DRAFT',
    visibility: 'PUBLIC',
    memberId: 1,
    thumbnailFile: null,
    thumbnailPreview: '',
    steps: [],
    categories: {},
    cookingTips: {}
});

const isValid = computed(() => {
    const basicValid = Boolean(form.title.trim());
    const stepsValid = form.steps.length > 0 && form.steps.every((s) => s.text.trim());
    const categoriesValid = categoryOptions.value.length === 0 || categoryOptions.value.every((option) => !!form.categories[option.codeId]);
    const cookingTipsValid = cookingTipsOptions.value.length === 0 || cookingTipsOptions.value.every((option) => !!form.cookingTips[option.codeId]);
    return basicValid && stepsValid && categoriesValid && cookingTipsValid;
});

onMounted(() => {
    const initializeRecipeEdit = async () => {
        await Promise.all([
            loadCategoryOptions(),
            loadCookingTipsOptions(),
            loadRecipeData()
        ]);
        initialLoading.value = false;
    };
    initializeRecipeEdit();
});

async function loadRecipeData() {
    try {
        const API_COOK_BASE_URL = import.meta.env.VITE_API_BASE_URL_COOK;
        const response = await httpJson(API_COOK_BASE_URL, `/api/recipe/${recipeId.value}`, {
            method: 'GET'
        });

        // 레시피 기본 정보 설정
        form.title = response.title || '';
        form.description = response.introduction || '';
        form.status = response.status || 'DRAFT';
        form.visibility = response.visibility || 'PUBLIC';
        form.memberId = response.memberId || 1;

        // 썸네일 설정 (메인 이미지)
        if (response.images && Array.isArray(response.images)) {
            const mainImage = response.images.find((img: any) => img.mainImage);
            if (mainImage) {
                form.thumbnailPreview = mainImage.url;
            }
        }

        // 카테고리 설정
        if (response.categories && Array.isArray(response.categories)) {
            response.categories.forEach((cat: any) => {
                form.categories[cat.codeId] = cat.detailCodeId;
            });
        }

        // 요리팁 설정
        if (response.cookingTips && Array.isArray(response.cookingTips)) {
            response.cookingTips.forEach((tip: any) => {
                form.cookingTips[tip.codeId] = tip.detailCodeId;
            });
        }

        // 단계 설정 (각 단계의 이미지 포함)
        if (response.steps && Array.isArray(response.steps)) {
            form.steps = response.steps.map((step: any) => {
                return {
                    id: crypto.randomUUID(),
                    file: null,
                    text: step.description || '',
                    previewUrl: step.image || '',
                    existingImageUrl: step.image || ''
                };
            });
        }
    } catch (e) {
        console.error('레시피 정보를 불러오지 못했습니다.', e);
        error.value = '레시피 정보를 불러오지 못했습니다.';
    }
}

async function loadCategoryOptions() {
    categoriesLoading.value = true;
    categoriesError.value = null;
    try {
        const response = await httpJson(import.meta.env.VITE_API_BASE_URL_COOK, '/api/common-codes?codeGroup=CATEGORY', {
            method: 'GET'
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

async function loadCookingTipsOptions() {
    cookingTipsLoading.value = true;
    cookingTipsError.value = null;
    try {
        const response = await httpJson(import.meta.env.VITE_API_BASE_URL_COOK, '/api/common-codes?codeGroup=COOKINGTIP', {
            method: 'GET'
        });

        if (Array.isArray(response)) {
            cookingTipsOptions.value = response;
            cookingTipsOptions.value.forEach((option) => {
                if (form.cookingTips[option.codeId] === undefined) {
                    form.cookingTips[option.codeId] = '';
                }
            });
        } else {
            cookingTipsOptions.value = [];
        }
    } catch (e) {
        console.error('요리팁 정보를 불러오지 못했습니다.', e);
        cookingTipsError.value = '요리팁 정보를 불러오지 못했습니다.';
    } finally {
        cookingTipsLoading.value = false;
    }
}

function addStep() {
    form.steps.push({ id: crypto.randomUUID(), file: null, text: '', previewUrl: '' });
}

function removeStep(index: number) {
    const [removed] = form.steps.splice(index, 1);
    if (removed && removed.previewUrl && !removed.existingImageUrl) {
        URL.revokeObjectURL(removed.previewUrl);
    }
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
    if (step.previewUrl && !step.existingImageUrl) {
        URL.revokeObjectURL(step.previewUrl);
    }
    step.previewUrl = URL.createObjectURL(file);
    step.existingImageUrl = undefined; // 새 파일 선택 시 기존 이미지 URL 제거
}

function onThumbnailChange(e: Event) {
    const input = e.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    form.thumbnailFile = file;
    if (form.thumbnailPreview && !form.thumbnailPreview.startsWith('http')) {
        URL.revokeObjectURL(form.thumbnailPreview);
    }
    form.thumbnailPreview = URL.createObjectURL(file);
}

function clearThumbnail() {
    if (form.thumbnailPreview && !form.thumbnailPreview.startsWith('http')) {
        URL.revokeObjectURL(form.thumbnailPreview);
    }
    form.thumbnailPreview = '';
    form.thumbnailFile = null;
    if (thumbnailInputRef.value) {
        thumbnailInputRef.value.value = '';
    }
}

function clearStepImage(step: RecipeStepDraft) {
    if (step.previewUrl && !step.existingImageUrl) {
        URL.revokeObjectURL(step.previewUrl);
    }
    step.previewUrl = '';
    step.file = null;
    step.existingImageUrl = undefined;
    const inputRef = stepInputRefs.value[step.id];
    if (inputRef) {
        inputRef.value = '';
    }
}

function goBack() {
    router.push('/my/recipes');
}

function buildRecipePayload() {
    const categories = categoryOptions.value
        .map((option) => ({
            codeId: option.codeId,
            detailCodeId: form.categories[option.codeId],
            codeGroup: 'CATEGORY'
        }))
        .filter((category) => Boolean(category.detailCodeId));

    const cookingTips = cookingTipsOptions.value
        .map((option) => ({
            codeId: option.codeId,
            detailCodeId: form.cookingTips[option.codeId],
            codeGroup: 'COOKINGTIP'
        }))
        .filter((cookingTip) => Boolean(cookingTip.detailCodeId));

    return {
        title: form.title,
        description: form.description,
        status: form.status,
        visibility: form.visibility,
        memberId: form.memberId,
        categories,
        cookingTips,
        steps: form.steps.map((s, idx) => ({ order: idx + 1, text: s.text.trim() }))
    };
}

async function submit() {
    if (!isValid.value) {
        error.value = '필수 항목을 모두 입력해주세요.';
        return;
    }
    submitting.value = true;
    error.value = null;
    try {
        // HttpOnly 쿠키를 통해 인증이 자동으로 처리되므로 토큰 검증 불필요

        // 멀티파트 폼 구성 (이미지 + 텍스트 단계)
        const formData = new FormData();

        const recipePayload = buildRecipePayload();

        // Blob을 사용하여 명시적으로 MIME 타입 설정
        const recipeBlob = new Blob([JSON.stringify(recipePayload)], {
            type: 'application/json; charset=utf-8'
        });
        formData.append('recipe', recipeBlob, 'recipe.json');

        // 이미지 변경 여부 확인
        const hasThumbnailChange = form.thumbnailFile !== null;
        const hasStepImageChanges = form.steps.some(s => s.file !== null);
        const hasAnyImageChange = hasThumbnailChange || hasStepImageChanges;

        // 이미지가 하나라도 변경되었을 때만 이미지 전송
        if (hasAnyImageChange) {
            
            // 썸네일 처리: 새 파일이 있으면 사용, 없으면 기존 URL에서 다운로드
            if (form.thumbnailFile) {
                formData.append('images', form.thumbnailFile, 'thumbnail.png');
            } else if (form.thumbnailPreview) {
                try {
                    const response = await fetch(form.thumbnailPreview, { credentials: 'include' });
                    if (response.ok) {
                        const blob = await response.blob();
                        formData.append('images', blob, 'thumbnail.png');
                    } else {
                        console.warn('썸네일 다운로드 실패:', response.status);
                    }
                } catch (err) {
                    console.error('썸네일 fetch 에러:', err);
                }
            }

            // 단계별 이미지 처리
            for (let i = 0; i < form.steps.length; i++) {
                const step = form.steps[i];
                if (step.file) {
                    formData.append('images', step.file, `step-${i + 1}.png`);
                } else if (step.existingImageUrl) {
                    try {
                        const response = await fetch(step.existingImageUrl, { credentials: 'include' });
                        if (response.ok) {
                            const blob = await response.blob();
                            formData.append('images', blob, `step-${i + 1}.png`);
                        } else {
                            console.warn(`Step ${i+1}: 이미지 다운로드 실패:`, response.status);
                        }
                    } catch (err) {
                        console.error(`Step ${i+1}: fetch 에러:`, err);
                    }
                }
            }

            // 대표 이미지 인덱스 설정
            formData.append('mainImageIndex', '0');
        }
        // 이미지 변경이 없으면 images를 전송하지 않음 -> 백엔드에서 기존 이미지 유지

        // 실제 API 엔드포인트로 전송 (토큰 자동 첨부) - PUT 메서드로 수정
        await httpForm(import.meta.env.VITE_API_BASE_URL_COOK, `/api/recipe/${recipeId.value}`, formData, { method: 'PUT' });

        alert('수정이 완료되었습니다.');
        router.push('/my/recipes');
    } catch (e) {
        console.error(e);
        error.value = e instanceof Error ? e.message : '레시피 수정 중 오류가 발생했습니다.';
    } finally {
        submitting.value = false;
    }
}

</script>

<style scoped></style>

