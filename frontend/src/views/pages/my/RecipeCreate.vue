<script setup lang="ts">
import RecipeFormBasicInfo from '@/components/recipe/form/RecipeFormBasicInfo.vue';
import RecipeFormClassification from '@/components/recipe/form/RecipeFormClassification.vue';
import RecipeFormIngredients from '@/components/recipe/form/RecipeFormIngredients.vue';
import RecipeFormSteps from '@/components/recipe/form/RecipeFormSteps.vue';
import { getCommonCodesByGroup, getCommonCodesByCodeId } from '@/api/commonCodeApi';
import { createRecipe } from '@/api/recipeApi';
import { fetchMemberInfo } from '@/utils/auth';
import { GUIDE_IMAGES } from '@/utils/constants';
import { useErrorHandler } from '@/utils/errorHandler';
import { useAppToast } from '@/utils/toast';
import type { CommonCodeOption, RecipeDraft } from '@/types/recipeForm';
import { nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue';
import { onBeforeRouteLeave, useRouter } from 'vue-router';
import Button from 'primevue/button';
import Popover from 'primevue/popover';
import Select from 'primevue/select';
import { useConfirm } from 'primevue/useconfirm';

const router = useRouter();
const confirm = useConfirm();
const { showError } = useAppToast();
const { handleApiCallVoid } = useErrorHandler({ showToast: true, showError });

// 반응형 상태
const submitting = ref(false);
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
const validationErrors = ref<Record<string, boolean>>({});
const basicInfoFormRef = ref<InstanceType<typeof RecipeFormBasicInfo> | null>(null);
const hasUnsavedChanges = ref(false);
const isSubmitSuccessful = ref(false);
const guideIconRefs = ref<Record<string, HTMLElement | null>>({});
const guidePopoverRefs = ref<Record<string, unknown>>({});

// 옵션 데이터
const visibilityOptions = [
    { label: '공개', value: 'PUBLIC' },
    { label: '비공개', value: 'PRIVATE' }
];

const statusOptions = [
    { label: '초안', value: 'DRAFT' },
    { label: '발행', value: 'PUBLISHED' }
];

const form = reactive<RecipeDraft>({
    title: '',
    description: '',
    status: 'DRAFT',
    visibility: 'PUBLIC',
    memberId: 0,
    thumbnailFile: null,
    thumbnailPreview: '',
    steps: [],
    ingredientGroups: [],
    categories: {},
    cookingTips: {}
});

// 유틸리티 함수
function clearImagePreview(previewUrl: string | undefined): void {
    if (previewUrl) URL.revokeObjectURL(previewUrl);
}

function onThumbnailUpdate(payload: { file: File; preview: string }): void {
    clearImagePreview(form.thumbnailPreview);
    form.thumbnailFile = payload.file;
    form.thumbnailPreview = payload.preview;
}

function clearThumbnail(): void {
    clearImagePreview(form.thumbnailPreview);
    form.thumbnailPreview = '';
    form.thumbnailFile = null;
}

function onStepImageChange(stepId: string, file: File, previewUrl: string): void {
    const step = form.steps.find((s) => s.id === stepId);
    if (step) {
        clearImagePreview(step.previewUrl);
        step.file = file;
        step.previewUrl = previewUrl;
    }
}

function onStepImageClear(stepId: string): void {
    const step = form.steps.find((s) => s.id === stepId);
    if (step) {
        clearImagePreview(step.previewUrl);
        step.previewUrl = '';
        step.file = null;
    }
}

// 검증 관련
function clearValidationError(key: string): void {
    if (validationErrors.value[key]) {
        delete validationErrors.value[key];
    }
}

function validateForm(): { valid: boolean; firstErrorField?: string; firstErrorFieldName?: string } {
    validationErrors.value = {};

    if (!form.title.trim()) {
        validationErrors.value.title = true;
        return { valid: false, firstErrorField: 'title', firstErrorFieldName: '제목' };
    }

    if (form.steps.length === 0) {
        return { valid: false, firstErrorField: 'steps', firstErrorFieldName: '조리 순서' };
    }

    for (let i = 0; i < form.steps.length; i++) {
        if (!form.steps[i].text.trim()) {
            validationErrors.value[`step-text-${i}`] = true;
            return {
                valid: false,
                firstErrorField: `step-text-${i}`,
                firstErrorFieldName: `조리 순서 ${i + 1}단계 설명`
            };
        }
    }

    // 카테고리: 7개 중 최소 1개만 선택하면 됨
    const hasAtLeastOneCategory = categoryOptions.value.length === 0 || categoryOptions.value.some((option) => !!form.categories[option.codeId]);
    if (!hasAtLeastOneCategory) {
        const firstOption = categoryOptions.value[0];
        if (firstOption) {
            validationErrors.value[`category-${firstOption.codeId}`] = true;
            return {
                valid: false,
                firstErrorField: `category-${firstOption.codeId}`,
                firstErrorFieldName: '카테고리를 최소 1개 이상 선택해주세요.'
            };
        }
    }

    for (const option of cookingTipsOptions.value) {
        if (!form.cookingTips[option.codeId]) {
            validationErrors.value[`cookingTip-${option.codeId}`] = true;
            return {
                valid: false,
                firstErrorField: `cookingTip-${option.codeId}`,
                firstErrorFieldName: `요리팁 - ${option.codeName}`
            };
        }
    }

    return { valid: true };
}

function focusFirstError(field: string): void {
    if (field === 'title') {
        nextTick(() => {
            basicInfoFormRef.value?.focusTitle?.();
        });
    } else if (field === 'steps') {
        nextTick(() => {
            // steps가 없을 때는 "단계 추가" 버튼에 focus
            const addStepButton = document.querySelector('[data-step-add-button]') as HTMLElement;
            if (addStepButton) {
                addStepButton.scrollIntoView({ behavior: 'smooth', block: 'center' });
                const buttonElement = addStepButton.querySelector('button') as HTMLElement;
                if (buttonElement && typeof buttonElement.focus === 'function') {
                    buttonElement.focus();
                }
            }
        });
    } else if (field.startsWith('step-text-')) {
        const stepIndex = parseInt(field.split('-')[2]);
        const stepElement = document.querySelector(`[data-step-index="${stepIndex}"]`);
        if (stepElement) {
            const textarea = stepElement.querySelector('textarea');
            textarea?.focus();
            stepElement.scrollIntoView({ behavior: 'smooth', block: 'center' });
        }
    } else if (field.startsWith('category-')) {
        nextTick(() => {
            const categoryElement = document.querySelector(`[data-category-id="${field}"]`);
            if (categoryElement) {
                categoryElement.scrollIntoView({ behavior: 'smooth', block: 'center' });
                // PrimeVue Select의 input 요소 찾기
                const selectInput = categoryElement.querySelector('.p-select .p-inputtext, .p-select input') as HTMLElement;
                if (selectInput && typeof selectInput.focus === 'function') {
                    selectInput.focus();
                } else {
                    // input을 찾지 못한 경우 Select 컨테이너 클릭으로 드롭다운 열기
                    const selectContainer = categoryElement.querySelector('.p-select') as HTMLElement;
                    if (selectContainer) {
                        selectContainer.click();
                    }
                }
            }
        });
    } else if (field.startsWith('cookingTip-')) {
        nextTick(() => {
            const cookingTipElement = document.querySelector(`[data-cooking-tip-id="${field}"]`);
            if (cookingTipElement) {
                cookingTipElement.scrollIntoView({ behavior: 'smooth', block: 'center' });
                // PrimeVue Select의 input 요소 찾기
                const selectInput = cookingTipElement.querySelector('.p-select .p-inputtext, .p-select input') as HTMLElement;
                if (selectInput && typeof selectInput.focus === 'function') {
                    selectInput.focus();
                } else {
                    // input을 찾지 못한 경우 Select 컨테이너 클릭으로 드롭다운 열기
                    const selectContainer = cookingTipElement.querySelector('.p-select') as HTMLElement;
                    if (selectContainer) {
                        selectContainer.click();
                    }
                }
            }
        });
    }
}

// 공통 코드 로딩 (codeGroup: CATEGORY, COOKINGTIP)
async function loadCommonCodes(codeGroup: 'CATEGORY' | 'COOKINGTIP'): Promise<CommonCodeOption[]> {
    try {
        return await getCommonCodesByGroup(codeGroup);
    } catch (e) {
        console.error(`${codeGroup} 정보를 불러오지 못했습니다.`, e);
        throw e;
    }
}

async function loadCategoryOptions(): Promise<void> {
    categoriesLoading.value = true;
    categoriesError.value = null;
    try {
        categoryOptions.value = await loadCommonCodes('CATEGORY');
        categoryOptions.value.forEach((option) => {
            if (form.categories[option.codeId] === undefined) {
                form.categories[option.codeId] = '';
            }
        });
    } catch {
        categoriesError.value = '카테고리 정보를 불러오지 못했습니다.';
    } finally {
        categoriesLoading.value = false;
    }
}

async function loadCookingTipsOptions(): Promise<void> {
    cookingTipsLoading.value = true;
    cookingTipsError.value = null;
    try {
        cookingTipsOptions.value = await loadCommonCodes('COOKINGTIP');
        cookingTipsOptions.value.forEach((option) => {
            if (form.cookingTips[option.codeId] === undefined) {
                form.cookingTips[option.codeId] = '';
            }
        });
    } catch {
        cookingTipsError.value = '요리팁 정보를 불러오지 못했습니다.';
    } finally {
        cookingTipsLoading.value = false;
    }
}

async function loadIngredientsGroupOptions(): Promise<void> {
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

async function loadIngredientsUnitOptions(): Promise<void> {
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

async function loadMemberInfo(): Promise<void> {
    const memberInfo = await fetchMemberInfo();
    if (memberInfo) {
        form.memberId = memberInfo.id;
    }
}

// 폼 제출 관련
function buildRecipePayload(statusOverride?: 'DRAFT' | 'PUBLISHED') {
    const categories = categoryOptions.value
        .map((option) => ({
            codeId: option.codeId,
            detailCodeId: form.categories[option.codeId],
            codeGroup: 'CATEGORY' as const
        }))
        .filter((category) => Boolean(category.detailCodeId));

    const cookingTips = cookingTipsOptions.value
        .map((option) => ({
            codeId: option.codeId,
            detailCodeId: form.cookingTips[option.codeId],
            codeGroup: 'COOKINGTIP' as const
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
        status: statusOverride ?? form.status,
        visibility: form.visibility,
        memberId: form.memberId,
        categories,
        cookingTips,
        ingredientGroups,
        steps: form.steps.map((s, idx) => ({
            order: idx + 1,
            text: s.text.trim()
        }))
    };
}

async function submit(): Promise<void> {
    const validation = validateForm();
    if (!validation.valid) {
        const fieldName = validation.firstErrorFieldName || '필수 항목';
        alert(`${fieldName}을(를) 입력해주세요.`);
        if (validation.firstErrorField) {
            setTimeout(() => {
                focusFirstError(validation.firstErrorField!);
            }, 100);
        }
        return;
    }

    submitting.value = true;

    const formData = new FormData();
    const recipePayload = buildRecipePayload();

    const recipeBlob = new Blob([JSON.stringify(recipePayload)], {
        type: 'application/json; charset=utf-8'
    });
    formData.append('recipe', recipeBlob, 'recipe.json');

    if (form.thumbnailFile) {
        formData.append('images', form.thumbnailFile, 'thumbnail.png');
    }

    form.steps.forEach((s, idx) => {
        if (s.file) {
            formData.append('images', s.file, `step-${idx + 1}.png`);
        }
    });

    formData.append('mainImageIndex', '0');

    const success = await handleApiCallVoid(() => createRecipe(formData), '레시피 등록 중 오류가 발생했습니다.', '등록 실패');

    if (success) {
        // 등록 성공 시 페이지 이탈 방지 해제
        isSubmitSuccessful.value = true;

        router.push('/my/recipes');
    } else {
        showError('레시피 등록 중 오류가 발생했습니다.', '등록 실패');
    }

    submitting.value = false;
}

function goBack(): void {
    router.push('/my/recipes');
}

// 가이드 이미지 매핑 (공통 상수 사용)
const guideImages = GUIDE_IMAGES;

// 가이드 표시
function showGuide(section: 'basic' | 'ingredients' | 'classification' | 'steps' | 'settings', event: Event): void {
    event.stopPropagation();
    const popover = guidePopoverRefs.value[section];
    if (popover && typeof popover.toggle === 'function') {
        popover.toggle(event);
    }
}

// 페이지 이탈 방지
watch(
    () => [form.title, form.description, form.thumbnailFile, form.steps.length, form.ingredientGroups.length, JSON.stringify(form.categories), JSON.stringify(form.cookingTips)],
    () => {
        // 어떤 필드라도 변경되면 unsaved changes로 표시
        if (form.title || form.description || form.thumbnailFile || form.steps.length > 0 || form.ingredientGroups.length > 0 || Object.values(form.categories).some((v) => v) || Object.values(form.cookingTips).some((v) => v)) {
            hasUnsavedChanges.value = true;
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
        message: '작성 중인 내용이 있습니다. 페이지를 나가시겠습니까?',
        header: '레시피 등록 나가기',
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

// 초기화
onMounted(() => {
    loadCategoryOptions();
    loadCookingTipsOptions();
    loadIngredientsGroupOptions();
    loadIngredientsUnitOptions();
    loadMemberInfo();

    // beforeunload 이벤트 리스너 등록
    window.addEventListener('beforeunload', handleBeforeUnload);
});

onBeforeUnmount(() => {
    // 컴포넌트 언마운트 시 이벤트 리스너 제거
    window.removeEventListener('beforeunload', handleBeforeUnload);
});
</script>

<template>
    <div class="card">
        <div class="flex items-center justify-between mb-4">
            <h2 class="text-2xl font-bold">레시피 등록</h2>
            <div class="flex gap-2">
                <Button label="목록으로" icon="pi pi-arrow-left" severity="secondary" @click="goBack" :disabled="submitting" />
            </div>
        </div>

        <div class="mb-6 p-4 bg-orange-50 border-l-4 border-orange-500 rounded-r">
            <p class="text-gray-700 italic">셰프님이 누군가를 위해 정성들인 이 요리처럼, 레시피에서도 셰프님의 따뜻한 정성을 보여주세요.</p>
        </div>

        <div class="flex flex-col gap-6">
            <RecipeFormBasicInfo
                ref="basicInfoFormRef"
                :title="form.title"
                :description="form.description"
                :thumbnail-preview="form.thumbnailPreview"
                :disabled="submitting"
                :validation-errors="validationErrors"
                :guide-image="guideImages.basic"
                @update:title="form.title = $event"
                @update:description="form.description = $event"
                @update:thumbnail="onThumbnailUpdate"
                @clear-thumbnail="clearThumbnail"
                @clear-validation="clearValidationError"
            />

            <RecipeFormIngredients
                v-model="form.ingredientGroups"
                :ingredient-type-options="ingredientTypeOptions"
                :unit-options="unitOptions"
                :ingredient-types-loading="ingredientTypesLoading"
                :ingredient-types-error="ingredientTypesError"
                :units-loading="unitsLoading"
                :units-error="unitsError"
                :validation-errors="validationErrors"
                :disabled="submitting"
                :guide-image="guideImages.ingredients"
                @clear-validation="clearValidationError"
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
                :validation-errors="validationErrors"
                :disabled="submitting"
                :guide-image="guideImages.classification"
                @update:categories="form.categories = $event"
                @update:cooking-tips="form.cookingTips = $event"
                @clear-validation="clearValidationError"
            />

            <RecipeFormSteps
                v-model="form.steps"
                :validation-errors="validationErrors"
                :disabled="submitting"
                :guide-image="guideImages.steps"
                @step-image-change="onStepImageChange"
                @step-image-clear="onStepImageClear"
                @clear-validation="clearValidationError"
            />

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
                    <Button label="등록" icon="pi pi-check" severity="primary" @click="submit" :disabled="submitting" />
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
