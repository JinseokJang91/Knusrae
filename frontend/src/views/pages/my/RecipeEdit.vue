<script setup lang="ts">
import RecipeFormBasicInfo from '@/components/recipe/form/RecipeFormBasicInfo.vue';
import RecipeFormClassification from '@/components/recipe/form/RecipeFormClassification.vue';
import RecipeFormIngredients from '@/components/recipe/form/RecipeFormIngredients.vue';
import RecipeFormSteps from '@/components/recipe/form/RecipeFormSteps.vue';
import { getCommonCodesByGroup, getCommonCodesByCodeId } from '@/api/commonCodeApi';
import { getRecipeDetail, updateRecipe } from '@/api/recipeApi';
import { GUIDE_IMAGES } from '@/utils/constants';
import { useErrorHandler } from '@/utils/errorHandler';
import { useAppToast } from '@/utils/toast';
import type { CommonCodeOption, RecipeDraft } from '@/types/recipeForm';
import type { RecipeCategory, RecipeCookingTip, RecipeImage, RecipeIngredientGroup, RecipeIngredientItem, RecipeStep } from '@/types/recipe';
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue';
import { onBeforeRouteLeave, useRoute, useRouter } from 'vue-router';
import { useConfirm } from 'primevue/useconfirm';
import Button from 'primevue/button';
import Message from 'primevue/message';
import Popover from 'primevue/popover';
import Select from 'primevue/select';

const router = useRouter();
const route = useRoute();
const recipeId = computed(() => Number(route.params.id));
const confirm = useConfirm();
const { showError } = useAppToast();
const { handleApiCallVoid } = useErrorHandler({ showToast: true, showError });

const initialLoading = ref(true);
const submitting = ref(false);
const error = ref<string | null>(null);
const categoriesLoading = ref(false);
const categoriesError = ref<string | null>(null);
const categoryOptions = ref<CommonCodeOption[]>([]);
const cookingTipsLoading = ref(false);
const cookingTipsError = ref<string | null>(null);
const cookingTipsOptions = ref<CommonCodeOption[]>([]);
const ingredientTypesLoading = ref(false);
const ingredientTypesError = ref<string | null>(null);
const ingredientTypeOptions = ref<CommonCodeOption[]>([]);
const unitsLoading = ref(false);
const unitsError = ref<string | null>(null);
const unitOptions = ref<CommonCodeOption[]>([]);
const hasUnsavedChanges = ref(false);
const isSubmitSuccessful = ref(false);
const originalFormData = ref<string>('');
const guideIconRefs = ref<Record<string, HTMLElement | null>>({});
const guidePopoverRefs = ref<Record<string, unknown>>({});

const form = reactive<RecipeDraft>({
    title: '',
    description: '',
    status: 'DRAFT',
    visibility: 'PUBLIC',
    memberId: 1,
    thumbnailFile: null,
    thumbnailPreview: '',
    steps: [],
    ingredientGroups: [],
    categories: {},
    cookingTips: {}
});

const isValid = computed(() => {
    const basicValid = Boolean(form.title.trim());
    const stepsValid = form.steps.length > 0 && form.steps.every((s) => s.text.trim());
    const categoriesValid = categoryOptions.value.length === 0 || categoryOptions.value.some((option) => !!form.categories[option.codeId]);
    const cookingTipsValid = cookingTipsOptions.value.length === 0 || cookingTipsOptions.value.every((option) => !!form.cookingTips[option.codeId]);
    return basicValid && stepsValid && categoriesValid && cookingTipsValid;
});

onMounted(() => {
    const initializeRecipeEdit = async () => {
        await Promise.all([loadCategoryOptions(), loadCookingTipsOptions(), loadIngredientsGroupOptions(), loadIngredientsUnitOptions(), loadRecipeData()]);
        initialLoading.value = false;
    };
    initializeRecipeEdit();

    // beforeunload 이벤트 리스너 등록
    window.addEventListener('beforeunload', handleBeforeUnload);
});

async function loadRecipeData() {
    try {
        const response = await getRecipeDetail(recipeId.value);

        // 레시피 기본 정보 설정
        form.title = response.title ?? '';
        form.description = response.introduction ?? '';
        form.status = response.status || 'DRAFT';
        form.visibility = response.visibility || 'PUBLIC';
        form.memberId = response.memberId || 1;

        // 썸네일 설정 (메인 이미지) — API는 mainImage 또는 isMainImage 반환
        if (response.images && Array.isArray(response.images)) {
            const mainImage = response.images.find((img: RecipeImage & { mainImage?: boolean }) => img.mainImage ?? img.isMainImage);
            if (mainImage) {
                form.thumbnailPreview = mainImage.url;
            }
        }

        // 카테고리 설정
        if (response.categories && Array.isArray(response.categories)) {
            (response.categories as RecipeCategory[]).forEach((cat: RecipeCategory) => {
                form.categories[cat.codeId] = cat.detailCodeId;
            });
        }

        // 요리팁 설정
        if (response.cookingTips && Array.isArray(response.cookingTips)) {
            (response.cookingTips as RecipeCookingTip[]).forEach((tip: RecipeCookingTip) => {
                form.cookingTips[tip.codeId] = tip.detailCodeId;
            });
        }

        // 준비물 설정
        if (response.ingredientGroups && Array.isArray(response.ingredientGroups)) {
            form.ingredientGroups = (response.ingredientGroups as RecipeIngredientGroup[]).map((group: RecipeIngredientGroup) => ({
                id: crypto.randomUUID(),
                type: group.detailCodeId || (group.customTypeName ? 'CUSTOM' : ''),
                customTypeName: group.customTypeName || '',
                items: Array.isArray(group.items)
                    ? (group.items as RecipeIngredientItem[]).map((item: RecipeIngredientItem) => ({
                          id: crypto.randomUUID(),
                          name: item.name || '',
                          quantity: item.quantity || null, // String으로 직접 저장 (분수 입력 지원)
                          unit: item.detailCodeId || (item.customUnitName ? 'CUSTOM' : ''),
                          customUnitName: item.customUnitName || ''
                      }))
                    : []
            }));
        }

        // 단계 설정 (각 단계의 이미지 포함) — API는 description, image 등 반환
        if (response.steps && Array.isArray(response.steps)) {
            const stepsFromApi = response.steps as (RecipeStep & { description?: string; image?: string })[];
            form.steps = stepsFromApi.map((step: RecipeStep & { description?: string; image?: string }) => {
                return {
                    id: crypto.randomUUID(),
                    file: null,
                    text: step.description ?? step.text ?? '',
                    previewUrl: step.image ?? step.imageUrl ?? '',
                    existingImageUrl: step.image ?? step.imageUrl ?? ''
                };
            });
        }

        // 원본 데이터 저장 (변경 감지용)
        originalFormData.value = JSON.stringify({
            title: form.title,
            description: form.description,
            status: form.status,
            visibility: form.visibility,
            thumbnailPreview: form.thumbnailPreview,
            steps: form.steps.map((s) => ({ text: s.text, existingImageUrl: s.existingImageUrl })),
            ingredientGroups: form.ingredientGroups,
            categories: form.categories,
            cookingTips: form.cookingTips
        });
    } catch (e) {
        console.error('레시피 정보를 불러오지 못했습니다.', e);
        error.value = '레시피 정보를 불러오지 못했습니다.';
    }
}

async function loadCategoryOptions() {
    categoriesLoading.value = true;
    categoriesError.value = null;
    try {
        categoryOptions.value = await getCommonCodesByGroup('CATEGORY');
        categoryOptions.value.forEach((option) => {
            if (form.categories[option.codeId] === undefined) {
                form.categories[option.codeId] = '';
            }
        });
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
        cookingTipsOptions.value = await getCommonCodesByGroup('COOKINGTIP');
        cookingTipsOptions.value.forEach((option) => {
            if (form.cookingTips[option.codeId] === undefined) {
                form.cookingTips[option.codeId] = '';
            }
        });
    } catch (e) {
        console.error('요리팁 정보를 불러오지 못했습니다.', e);
        cookingTipsError.value = '요리팁 정보를 불러오지 못했습니다.';
    } finally {
        cookingTipsLoading.value = false;
    }
}

async function loadIngredientsGroupOptions() {
    ingredientTypesLoading.value = true;
    ingredientTypesError.value = null;
    try {
        ingredientTypeOptions.value = await getCommonCodesByCodeId('INGREDIENTS_GROUP');
    } catch {
        ingredientTypesError.value = '재료 타입 정보를 불러오지 못했습니다.';
    } finally {
        ingredientTypesLoading.value = false;
    }
}

async function loadIngredientsUnitOptions() {
    unitsLoading.value = true;
    unitsError.value = null;
    try {
        unitOptions.value = await getCommonCodesByCodeId('INGREDIENTS_UNIT');
    } catch {
        unitsError.value = '단위 정보를 불러오지 못했습니다.';
    } finally {
        unitsLoading.value = false;
    }
}

const visibilityOptions = [
    { label: '공개', value: 'PUBLIC' },
    { label: '비공개', value: 'PRIVATE' }
];

const statusOptions = [
    { label: '초안', value: 'DRAFT' },
    { label: '발행', value: 'PUBLISHED' }
];

function isObjectUrl(url: string): boolean {
    return url.startsWith('blob:');
}

function onThumbnailUpdate(payload: { file: File; preview: string }): void {
    if (form.thumbnailPreview && isObjectUrl(form.thumbnailPreview)) {
        URL.revokeObjectURL(form.thumbnailPreview);
    }
    form.thumbnailFile = payload.file;
    form.thumbnailPreview = payload.preview;
}

function clearThumbnail(): void {
    if (form.thumbnailPreview && isObjectUrl(form.thumbnailPreview)) {
        URL.revokeObjectURL(form.thumbnailPreview);
    }
    form.thumbnailPreview = '';
    form.thumbnailFile = null;
}

function onStepImageChange(stepId: string, file: File, previewUrl: string): void {
    const step = form.steps.find((s) => s.id === stepId);
    if (step) {
        if (step.previewUrl && !step.existingImageUrl) {
            URL.revokeObjectURL(step.previewUrl);
        }
        step.file = file;
        step.previewUrl = previewUrl;
        step.existingImageUrl = undefined;
    }
}

function onStepImageClear(stepId: string): void {
    const step = form.steps.find((s) => s.id === stepId);
    if (step) {
        if (step.previewUrl && !step.existingImageUrl) {
            URL.revokeObjectURL(step.previewUrl);
        }
        step.previewUrl = '';
        step.file = null;
        step.existingImageUrl = undefined;
    }
}

function goBack() {
    router.push('/my/recipes');
}

// 가이드 이미지 매핑 (공통 상수 사용)
const guideImages = GUIDE_IMAGES;

// 가이드 표시
function showGuide(section: 'basic' | 'ingredients' | 'classification' | 'steps' | 'settings', event: Event): void {
    event.stopPropagation();
    const popover = guidePopoverRefs.value[section] as { toggle?: (e: Event) => void } | undefined;
    if (popover?.toggle) {
        popover.toggle(event);
    }
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

    const ingredientGroups = form.ingredientGroups
        .filter((group) => group.type && group.items.length > 0)
        .map((group, groupIdx) => ({
            codeId: group.type !== 'CUSTOM' ? 'INGREDIENTS_GROUP' : null,
            detailCodeId: group.type !== 'CUSTOM' ? group.type : null,
            customTypeName: group.type === 'CUSTOM' ? group.customTypeName : null,
            order: groupIdx + 1,
            items: group.items
                .filter((item) => item.name.trim()) // 이름만 필수, 수량과 단위는 선택사항
                .map((item, idx) => ({
                    order: idx + 1,
                    name: item.name.trim(),
                    quantity: item.quantity && item.quantity.trim() ? item.quantity.trim() : null, // null 허용 (분수 입력 지원: "1/2", "3/4", "1.5" 등)
                    codeId: item.unit && item.unit !== 'CUSTOM' ? 'INGREDIENTS_UNIT' : null,
                    detailCodeId: item.unit && item.unit !== 'CUSTOM' ? item.unit : null,
                    customUnitName: item.unit === 'CUSTOM' ? item.customUnitName : null
                }))
        }))
        .filter((group) => group.items.length > 0);

    return {
        title: form.title,
        description: form.description,
        status: form.status,
        visibility: form.visibility,
        memberId: form.memberId,
        categories,
        cookingTips,
        ingredientGroups,
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
        const hasStepImageChanges = form.steps.some((s) => s.file !== null);
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
                        }
                    } catch (err) {
                        console.error(`Step ${i + 1}: fetch 에러:`, err);
                    }
                }
            }

            // 대표 이미지 인덱스 설정
            formData.append('mainImageIndex', '0');
        }
        // 이미지 변경이 없으면 images를 전송하지 않음 -> 백엔드에서 기존 이미지 유지

        // 실제 API 엔드포인트로 전송 (토큰 자동 첨부) - PUT 메서드로 수정
        const success = await handleApiCallVoid(() => updateRecipe(recipeId.value, formData), '레시피 수정 중 오류가 발생했습니다.', '수정 실패');

        if (success) {
            // 수정 성공 시 페이지 이탈 방지 해제
            isSubmitSuccessful.value = true;

            alert('수정이 완료되었습니다.');
            router.push('/my/recipes');
        } else {
            error.value = '레시피 수정 중 오류가 발생했습니다.';
        }
    } finally {
        submitting.value = false;
    }
}

// 페이지 이탈 방지
watch(
    () => [form.title, form.description, form.status, form.visibility, form.thumbnailFile, form.steps.length, form.ingredientGroups.length, JSON.stringify(form.categories), JSON.stringify(form.cookingTips)],
    () => {
        // 초기 로딩이 완료되고 원본 데이터가 있을 때만 비교
        if (originalFormData.value && !initialLoading.value) {
            const original = JSON.parse(originalFormData.value);

            // 파일 변경 감지
            const hasFileChange = Boolean(form.thumbnailFile) || form.steps.some((s) => s.file);

            // 데이터 변경 감지
            const hasDataChange =
                form.title !== original.title ||
                form.description !== original.description ||
                form.status !== original.status ||
                form.visibility !== original.visibility ||
                JSON.stringify(form.categories) !== JSON.stringify(original.categories) ||
                JSON.stringify(form.cookingTips) !== JSON.stringify(original.cookingTips) ||
                JSON.stringify(form.ingredientGroups) !== JSON.stringify(original.ingredientGroups) ||
                form.steps.length !== original.steps.length ||
                form.steps.some((s, i) => s.text !== original.steps[i]?.text);

            hasUnsavedChanges.value = hasFileChange || hasDataChange;
        }
    },
    { deep: true }
);

// 브라우저 새로고침/닫기 방지
function handleBeforeUnload(e: BeforeUnloadEvent): void {
    if (hasUnsavedChanges.value && !isSubmitSuccessful.value) {
        e.preventDefault();
        e.returnValue = '';
    }
}

// Vue Router 페이지 이탈 방지
onBeforeRouteLeave((_to, _from, next) => {
    if (!hasUnsavedChanges.value || isSubmitSuccessful.value) {
        next();
        return;
    }

    confirm.require({
        message: '수정 중인 내용이 있습니다. 페이지를 나가시겠습니까?',
        header: '레시피 수정 나가기',
        icon: 'pi pi-exclamation-triangle',
        rejectLabel: '취소',
        acceptLabel: '나가기',
        acceptClass: 'p-button-danger',
        accept: () => {
            next();
        },
        reject: () => {
            next(false);
        }
    });
});

onBeforeUnmount(() => {
    // 컴포넌트 언마운트 시 이벤트 리스너 제거
    window.removeEventListener('beforeunload', handleBeforeUnload);
});
</script>

<template>
    <div class="card">
        <div class="flex items-center justify-between mb-4">
            <h2 class="text-2xl font-bold">레시피 수정</h2>
            <div class="flex gap-2">
                <Button label="목록으로" icon="pi pi-arrow-left" severity="secondary" @click="goBack" :disabled="submitting" />
            </div>
        </div>

        <!-- 에러 메시지 표시 -->
        <Message v-if="error" severity="error" :closable="false" class="mb-4">
            {{ error }}
        </Message>

        <!-- 로딩 상태 -->
        <div v-if="initialLoading" class="flex justify-center items-center py-8">
            <div class="pi pi-spinner pi-spin mr-2"></div>
            <span>레시피 정보를 불러오는 중...</span>
        </div>

        <div v-else class="flex flex-col gap-6">
            <div class="mb-6 p-4 bg-orange-50 border-l-4 border-orange-500 rounded-r">
                <p class="text-gray-700 italic">셰프님이 누군가를 위해 정성들인 이 요리처럼, 레시피에서도 셰프님의 따뜻한 정성을 보여주세요.</p>
            </div>

            <RecipeFormBasicInfo
                :title="form.title"
                :description="form.description"
                :thumbnail-preview="form.thumbnailPreview || ''"
                :disabled="submitting"
                :guide-image="guideImages.basic"
                @update:title="form.title = $event"
                @update:description="form.description = $event"
                @update:thumbnail="onThumbnailUpdate"
                @clear-thumbnail="clearThumbnail"
            />

            <RecipeFormIngredients
                v-model="form.ingredientGroups"
                :ingredient-type-options="ingredientTypeOptions"
                :unit-options="unitOptions"
                :ingredient-types-loading="ingredientTypesLoading"
                :ingredient-types-error="ingredientTypesError"
                :units-loading="unitsLoading"
                :units-error="unitsError"
                :disabled="submitting"
                :guide-image="guideImages.ingredients"
            />

            <RecipeFormClassification
                :category-options="categoryOptions"
                :categories="form.categories"
                :categories-loading="categoriesLoading"
                :categories-error="categoriesError"
                :cooking-tips-options="cookingTipsOptions"
                :cooking-tips="form.cookingTips"
                :cooking-tips-loading="cookingTipsLoading"
                :cooking-tips-error="cookingTipsError"
                :disabled="submitting"
                :guide-image="guideImages.classification"
                @update:categories="form.categories = $event"
                @update:cooking-tips="form.cookingTips = $event"
            />

            <RecipeFormSteps v-model="form.steps" :disabled="submitting" :guide-image="guideImages.steps" @step-image-change="onStepImageChange" @step-image-clear="onStepImageClear" />

            <!-- 설정 및 저장 -->
            <div class="border border-gray-200 rounded-lg p-5 bg-white">
                <div class="flex items-center gap-1 mb-1">
                    <h3 class="text-xl font-semibold text-gray-600">
                        <span class="mr-1">설정 및 저장</span>
                        <i ref="el => { if (el) guideIconRefs.settings = el as HTMLElement; }" class="pi pi-question-circle help-button" @click="showGuide('settings', $event)" style="cursor: pointer" />
                        <Popover
                            :ref="
                                (el) => {
                                    if (el) guidePopoverRefs.settings = el;
                                }
                            "
                            :target="guideIconRefs.settings"
                            :showCloseIcon="true"
                            :dismissable="true"
                        >
                            <div class="p-2">
                                <img :src="guideImages.settings" alt="설정 및 저장 가이드" class="max-w-full h-auto" />
                            </div>
                        </Popover>
                    </h3>
                </div>
                <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-4">
                    <div>
                        <label class="block mb-2 font-medium"><b>공개 여부</b></label>
                        <Select v-model="form.visibility" :options="visibilityOptions" optionLabel="label" optionValue="value" class="w-full" />
                    </div>
                    <div>
                        <label class="block mb-2 font-medium"><b>상태</b></label>
                        <Select v-model="form.status" :options="statusOptions" optionLabel="label" optionValue="value" class="w-full" />
                    </div>
                </div>
                <div class="flex justify-end gap-2">
                    <Button label="취소" icon="pi pi-times" severity="secondary" @click="goBack" :disabled="submitting" />
                    <Button label="수정" icon="pi pi-check" severity="primary" @click="submit" :disabled="submitting || !isValid" />
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
:deep(.p-textarea) {
    resize: none;
    overflow-y: auto;
}

:deep(.p-autocomplete.border-red-500 .p-inputtext),
:deep(.p-inputtext.border-red-500),
:deep(.p-textarea.border-red-500),
:deep(.p-select.border-red-500 .p-inputtext) {
    border-color: #ef4444 !important;
    box-shadow: 0 0 0 0.2rem rgba(239, 68, 68, 0.2) !important;
}

.help-button {
    color: #ea580c;
    opacity: 0.7;
    transition: opacity 0.2s;
}

.help-button:hover {
    opacity: 1;
}
</style>
