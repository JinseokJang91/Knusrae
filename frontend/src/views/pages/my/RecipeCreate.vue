<template>
    <div class="card">
        <div class="flex items-center justify-between mb-4">
            <h2 class="text-2xl font-bold">레시피 등록</h2>
            <div class="flex gap-2">
                <Button label="목록으로" icon="pi pi-arrow-left" severity="secondary" @click="goBack" :disabled="submitting" />
            </div>
        </div>

        <div class="flex flex-col gap-6">
            <!-- 기본 정보: 대표 사진, 제목, 소개 -->
            <div class="border border-gray-200 rounded-lg p-5 bg-white">
                <h3 class="text-lg font-semibold mb-4 text-green-600">기본 정보</h3>
                <div class="grid grid-cols-1 md:grid-cols-5 gap-4">
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
                                <Button 
                                    icon="pi pi-times"
                                    severity="danger"
                                    rounded
                                    size="small"
                                    class="absolute top-2 right-2"
                                    @click.stop="clearThumbnail"
                                    :disabled="submitting"
                                />
                            </div>
                        </div>
                        <p class="text-sm text-gray-500 mt-1">등록 시 썸네일이 대표 이미지로 사용됩니다.</p>
                    </div>

                    <div class="md:col-span-3 flex flex-col gap-4">
                        <div>
                            <label class="block mb-2 font-medium"><b>제목</b></label>
                            <InputText 
                                ref="titleInputRef"
                                v-model.trim="form.title" 
                                placeholder="레시피 제목을 입력하세요" 
                                class="w-full"
                                :class="{ 'border-red-500': validationErrors.title }"
                                @input="clearValidationError('title')"
                            />
                        </div>
                        <div class="flex-1">
                            <label class="block mb-2 font-medium"><b>소개</b></label>
                            <Textarea v-model.trim="form.description" placeholder="간단한 소개나 메모를 작성하세요" class="w-full" :rows="9" />
                        </div>
                    </div>
                </div>
            </div>

            <!-- 준비물 -->
            <div class="border border-gray-200 rounded-lg p-5 bg-white">
                <div class="flex items-center justify-between mb-4">
                    <h3 class="text-lg font-semibold text-green-600">준비물</h3>
                    <Button label="그룹 추가" icon="pi pi-plus" @click="addIngredientGroup" :disabled="submitting" />
                </div>
                <div v-if="form.ingredientGroups.length === 0" class="p-3 text-gray-500 border rounded">
                    아직 준비물 그룹이 없습니다. '그룹 추가'를 눌러 시작하세요.
                </div>

                <div v-for="(group, groupIndex) in form.ingredientGroups" :key="group.id" class="border rounded p-4 mb-4 bg-gray-50">
                    <div class="flex items-center justify-between mb-4">
                        <div class="flex items-center gap-3 flex-1">
                            <div class="font-medium text-gray-700">그룹 {{ groupIndex + 1 }}</div>
                            <div class="flex-1 max-w-xs">
                                <Select 
                                    v-model="group.type" 
                                    :options="ingredientTypeOptions" 
                                    optionLabel="label" 
                                    optionValue="value"
                                    placeholder="주제를 선택하세요"
                                    class="w-full"
                                    :class="{ 'border-red-500': validationErrors[`group-type-${group.id}`] }"
                                    @change="clearValidationError(`group-type-${group.id}`)"
                                    filter
                                />
                            </div>
                        </div>
                        <div class="flex gap-2">
                            <Button icon="pi pi-arrow-up" severity="secondary" size="small" @click="moveIngredientGroupUp(groupIndex)" :disabled="groupIndex === 0 || submitting" />
                            <Button icon="pi pi-arrow-down" severity="secondary" size="small" @click="moveIngredientGroupDown(groupIndex)" :disabled="groupIndex === form.ingredientGroups.length - 1 || submitting" />
                            <Button icon="pi pi-trash" severity="danger" size="small" @click="removeIngredientGroup(groupIndex)" :disabled="submitting" />
                        </div>
                    </div>

                    <div class="mb-3">
                        <Button label="항목 추가" icon="pi pi-plus" size="small" @click="addIngredientItem(groupIndex)" :disabled="submitting" />
                    </div>

                    <div v-if="group.items.length === 0" class="p-2 text-sm text-gray-400 border border-dashed rounded mb-2">
                        이 그룹에 항목이 없습니다. '항목 추가'를 눌러 추가하세요.
                    </div>

                    <div v-for="(item, itemIndex) in group.items" :key="item.id" class="border rounded p-3 mb-2 bg-white">
                        <div class="flex items-center justify-between mb-2">
                            <div class="text-sm text-gray-600">항목 {{ itemIndex + 1 }}</div>
                            <div class="flex gap-2">
                                <Button icon="pi pi-arrow-up" severity="secondary" size="small" @click="moveIngredientItemUp(groupIndex, itemIndex)" :disabled="itemIndex === 0 || submitting" />
                                <Button icon="pi pi-arrow-down" severity="secondary" size="small" @click="moveIngredientItemDown(groupIndex, itemIndex)" :disabled="itemIndex === group.items.length - 1 || submitting" />
                                <Button icon="pi pi-trash" severity="danger" size="small" @click="removeIngredientItem(groupIndex, itemIndex)" :disabled="submitting" />
                            </div>
                        </div>
                        <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                            <div>
                                <label class="block mb-2 text-sm">이름</label>
                                <InputText v-model.trim="item.name" placeholder="재료명을 입력하세요" class="w-full" />
                            </div>
                            <div>
                                <label class="block mb-2 text-sm">수량</label>
                                <InputNumber v-model="item.quantity" placeholder="수량을 입력하세요" class="w-full" :min="0" :maxFractionDigits="2" />
                            </div>
                            <div>
                                <label class="block mb-2 text-sm">단위</label>
                                <Select 
                                    v-model="item.unit" 
                                    :options="unitOptions" 
                                    optionLabel="label" 
                                    optionValue="value"
                                    placeholder="단위를 선택하세요"
                                    class="w-full"
                                    :class="{ 'border-red-500': validationErrors[`item-unit-${item.id}`] }"
                                    @change="clearValidationError(`item-unit-${item.id}`)"
                                    filter
                                />
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 분류 정보: 카테고리, 요리팁 -->
            <div class="border border-gray-200 rounded-lg p-5 bg-white">
                <h3 class="text-lg font-semibold mb-4 text-green-600">분류 정보</h3>
                <div class="flex flex-col gap-6">
                    <div>
                        <label class="block mb-2 font-medium"><b>카테고리</b></label>
                        <Message v-if="categoriesError" severity="error" :closable="false" class="mb-2">
                            {{ categoriesError }}
                        </Message>
                        <div v-else>
                            <div v-if="categoriesLoading" class="p-3 text-gray-500 border border-dashed rounded">
                                카테고리 정보를 불러오는 중입니다...
                            </div>
                            <div v-else class="grid grid-cols-1 md:grid-cols-3 gap-4">
                                <div v-for="option in categoryOptions" :key="option.codeId" class="flex flex-col gap-2" :data-category-id="`category-${option.codeId}`">
                                    <span class="text-sm font-medium text-gray-700">{{ option.codeName }}</span>
                                    <Select 
                                        v-model="form.categories[option.codeId]" 
                                        :options="getCommonCodeDetailOptions(option)" 
                                        optionLabel="label" 
                                        optionValue="value"
                                        placeholder="선택하세요"
                                        class="w-full"
                                        :class="{ 'border-red-500': validationErrors[`category-${option.codeId}`] }"
                                        @change="clearValidationError(`category-${option.codeId}`)"
                                    />
                                </div>
                            </div>
                        </div>
                    </div>

                    <div>
                        <label class="block mb-2 font-medium"><b>요리팁</b></label>
                        <Message v-if="cookingTipsError" severity="error" :closable="false" class="mb-2">
                            {{ cookingTipsError }}
                        </Message>
                        <div v-else>
                            <div v-if="cookingTipsLoading" class="p-3 text-gray-500 border border-dashed rounded">
                                요리팁 정보를 불러오는 중입니다...
                            </div>
                            <div v-else class="grid grid-cols-1 md:grid-cols-3 gap-4">
                                <div v-for="option in cookingTipsOptions" :key="option.codeId" class="flex flex-col gap-2" :data-cooking-tip-id="`cookingTip-${option.codeId}`">
                                    <span class="text-sm font-medium text-gray-700">{{ option.codeName }}</span>
                                    <Select 
                                        v-model="form.cookingTips[option.codeId]" 
                                        :options="getCommonCodeDetailOptions(option)" 
                                        optionLabel="label" 
                                        optionValue="value"
                                        placeholder="선택하세요"
                                        class="w-full"
                                        :class="{ 'border-red-500': validationErrors[`cookingTip-${option.codeId}`] }"
                                        @change="clearValidationError(`cookingTip-${option.codeId}`)"
                                    />
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 조리 순서 -->
            <div class="border border-gray-200 rounded-lg p-5 bg-white">
                <div class="flex items-center justify-between mb-4">
                    <h3 class="text-lg font-semibold text-green-600">조리 순서</h3>
                    <div data-step-add-button>
                        <Button label="단계 추가" icon="pi pi-plus" @click="addStep" :disabled="submitting" />
                    </div>
                </div>
                <div v-if="form.steps.length === 0" class="p-3 text-gray-500 border rounded">
                    아직 단계가 없습니다. '단계 추가'를 눌러 시작하세요.
                </div>

                <div v-for="(step, index) in form.steps" :key="step.id" class="border rounded p-3 mb-3 bg-gray-50" :data-step-index="index">
                    <div class="flex items-center justify-between mb-3">
                        <div class="font-medium">단계 {{ index + 1 }}</div>
                        <div class="flex gap-2">
                            <Button icon="pi pi-arrow-up" severity="secondary" size="small" @click="moveStepUp(index)" :disabled="index === 0 || submitting" />
                            <Button icon="pi pi-arrow-down" severity="secondary" size="small" @click="moveStepDown(index)" :disabled="index === form.steps.length - 1 || submitting" />
                            <Button icon="pi pi-trash" severity="danger" size="small" @click="removeStep(index)" :disabled="submitting" />
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
                                    <Button 
                                        icon="pi pi-times"
                                        severity="danger"
                                        rounded
                                        size="small"
                                        class="absolute top-2 right-2"
                                        @click.stop="clearStepImage(step)"
                                        :disabled="submitting"
                                    />
                                </div>
                            </div>
                        </div>
                        <div class="md:col-span-3">
                            <label class="block mb-2">설명</label>
                            <Textarea 
                                v-model.trim="step.text" 
                                placeholder="이 단계에서의 설명을 작성하세요" 
                                class="w-full" 
                                :class="{ 'border-red-500': validationErrors[`step-text-${index}`] }"
                                :rows="9"
                                @input="clearValidationError(`step-text-${index}`)"
                            />
                        </div>
                    </div>
                </div>
            </div>

            <!-- 설정 및 저장 -->
            <div class="border border-gray-200 rounded-lg p-5 bg-white">
                <h3 class="text-lg font-semibold mb-4 text-green-600">설정 및 저장</h3>
                <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-4">
                    <div>
                        <label class="block mb-2 font-medium"><b>공개 여부</b></label>
                        <Select 
                            v-model="form.visibility" 
                            :options="visibilityOptions" 
                            optionLabel="label" 
                            optionValue="value"
                            class="w-full"
                        />
                    </div>
                    <div>
                        <label class="block mb-2 font-medium"><b>상태</b></label>
                        <Select 
                            v-model="form.status" 
                            :options="statusOptions" 
                            optionLabel="label" 
                            optionValue="value"
                            class="w-full"
                        />
                    </div>
                </div>
                <div class="flex justify-end gap-2">
                    <Button label="초안 저장" icon="pi pi-save" severity="secondary" @click="saveAsDraft" :disabled="submitting" />
                    <Button label="등록" icon="pi pi-check" severity="success" @click="submit" :disabled="submitting" />
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { httpForm, httpJson } from '@/utils/http';
import { fetchMemberInfo } from '@/utils/auth';
import { nextTick, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import Button from 'primevue/button';
import InputNumber from 'primevue/inputnumber';
import InputText from 'primevue/inputtext';
import Message from 'primevue/message';
import Select from 'primevue/select';
import Textarea from 'primevue/textarea';
import { useToast } from 'primevue/usetoast';

const router = useRouter();
const toast = useToast();

// 타입 정의
interface RecipeStepDraft {
    id: string;
    file?: File | null;
    previewUrl?: string;
    text: string;
}

interface IngredientItemDraft {
    id: string;
    name: string;
    quantity: number | null;
    unit: string;
}

interface IngredientGroupDraft {
    id: string;
    type: string;
    items: IngredientItemDraft[];
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
    ingredientGroups: IngredientGroupDraft[];
    categories: Record<string, string>;
    cookingTips: Record<string, string>;
}

// 반응형 상태
const submitting = ref(false);
const categoriesLoading = ref(false);
const categoriesError = ref<string | null>(null);
const categoryOptions = ref<CommonCodeOption[]>([]);
const cookingTipsLoading = ref(false);
const cookingTipsError = ref<string | null>(null);
const cookingTipsOptions = ref<CommonCodeOption[]>([]);
const thumbnailInputRef = ref<HTMLInputElement | null>(null);
const stepInputRefs = ref<Record<string, HTMLInputElement>>({});
const titleInputRef = ref<InstanceType<typeof InputText> | null>(null);
const validationErrors = ref<Record<string, boolean>>({});

// 옵션 데이터
const visibilityOptions = [
    { label: '공개', value: 'PUBLIC' },
    { label: '비공개', value: 'PRIVATE' }
];

const statusOptions = [
    { label: '초안', value: 'DRAFT' },
    { label: '발행', value: 'PUBLISHED' }
];

const ingredientTypeOptions = [
    { label: '재료', value: 'INGREDIENT' },
    { label: '양념', value: 'SEASONING' },
    { label: '조리도구', value: 'TOOL' },
    { label: '기타', value: 'OTHER' }
];

const unitOptions = [
    { label: 'g (그램)', value: 'g' },
    { label: 'kg (킬로그램)', value: 'kg' },
    { label: 'ml (밀리리터)', value: 'ml' },
    { label: 'L (리터)', value: 'L' },
    { label: '개', value: '개' },
    { label: '마리', value: '마리' },
    { label: '잔', value: '잔' },
    { label: '큰술', value: '큰술' },
    { label: '작은술', value: '작은술' },
    { label: '컵', value: '컵' },
    { label: '봉지', value: '봉지' },
    { label: '팩', value: '팩' },
    { label: '줄기', value: '줄기' },
    { label: '쪽', value: '쪽' },
    { label: '장', value: '장' },
    { label: '조각', value: '조각' },
    { label: '토막', value: '토막' },
    { label: '적당량', value: '적당량' }
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
function generateId(): string {
    return crypto.randomUUID();
}

function swapArrayItems<T>(array: T[], index1: number, index2: number): void {
    if (index1 < 0 || index2 < 0 || index1 >= array.length || index2 >= array.length) return;
    [array[index1], array[index2]] = [array[index2], array[index1]];
}

function handleImageFile(file: File | undefined, currentPreview: string | undefined): string {
    if (!file) return currentPreview || '';
    if (currentPreview) URL.revokeObjectURL(currentPreview);
    return URL.createObjectURL(file);
}

function clearImagePreview(previewUrl: string | undefined): void {
    if (previewUrl) URL.revokeObjectURL(previewUrl);
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

    for (const option of categoryOptions.value) {
        if (!form.categories[option.codeId]) {
            validationErrors.value[`category-${option.codeId}`] = true;
            return { 
                valid: false, 
                firstErrorField: `category-${option.codeId}`, 
                firstErrorFieldName: `카테고리 - ${option.codeName}` 
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
            if (titleInputRef.value) {
                const component = titleInputRef.value as any;
                const inputElement = component.$el?.querySelector('input') || component.$el;
                if (inputElement && typeof inputElement.focus === 'function') {
                    inputElement.focus();
                    inputElement.scrollIntoView({ behavior: 'smooth', block: 'center' });
                }
            }
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

// 공통 코드 로딩
async function loadCommonCodes(codeGroup: 'CATEGORY' | 'COOKINGTIP'): Promise<CommonCodeOption[]> {
    try {
        const response = await httpJson(
            import.meta.env.VITE_API_BASE_URL_COOK,
            `/api/common-codes?codeGroup=${codeGroup}`,
            { method: 'GET' }
        );
        return Array.isArray(response) ? response : [];
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
    } catch (e) {
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
    } catch (e) {
        cookingTipsError.value = '요리팁 정보를 불러오지 못했습니다.';
    } finally {
        cookingTipsLoading.value = false;
    }
}

async function loadMemberInfo(): Promise<void> {
    const memberInfo = await fetchMemberInfo();
    if (memberInfo) {
        form.memberId = memberInfo.id;
    }
}

// 공통 코드 옵션 변환
function getCommonCodeDetailOptions(option: CommonCodeOption) {
    return [
        { label: '선택하세요', value: '' },
        ...option.details.map(detail => ({
            label: detail.codeName,
            value: detail.detailCodeId
        }))
    ];
}

// 준비물 관련 함수
function addIngredientGroup(): void {
    form.ingredientGroups.push({ 
        id: generateId(), 
        type: '',
        items: []
    });
}

function removeIngredientGroup(groupIndex: number): void {
    form.ingredientGroups.splice(groupIndex, 1);
}

function moveIngredientGroupUp(groupIndex: number): void {
    if (groupIndex > 0) {
        swapArrayItems(form.ingredientGroups, groupIndex - 1, groupIndex);
    }
}

function moveIngredientGroupDown(groupIndex: number): void {
    if (groupIndex < form.ingredientGroups.length - 1) {
        swapArrayItems(form.ingredientGroups, groupIndex, groupIndex + 1);
    }
}

function addIngredientItem(groupIndex: number): void {
    const group = form.ingredientGroups[groupIndex];
    if (group) {
        group.items.push({ 
            id: generateId(), 
            name: '', 
            quantity: null, 
            unit: '' 
        });
    }
}

function removeIngredientItem(groupIndex: number, itemIndex: number): void {
    const group = form.ingredientGroups[groupIndex];
    if (group) {
        group.items.splice(itemIndex, 1);
    }
}

function moveIngredientItemUp(groupIndex: number, itemIndex: number): void {
    const group = form.ingredientGroups[groupIndex];
    if (group && itemIndex > 0) {
        swapArrayItems(group.items, itemIndex - 1, itemIndex);
    }
}

function moveIngredientItemDown(groupIndex: number, itemIndex: number): void {
    const group = form.ingredientGroups[groupIndex];
    if (group && itemIndex < group.items.length - 1) {
        swapArrayItems(group.items, itemIndex, itemIndex + 1);
    }
}

// 조리 순서 관련 함수
function addStep(): void {
    form.steps.push({ 
        id: generateId(), 
        file: null, 
        text: '', 
        previewUrl: '' 
    });
}

function removeStep(index: number): void {
    const [removed] = form.steps.splice(index, 1);
    if (removed?.previewUrl) {
        clearImagePreview(removed.previewUrl);
    }
}

function moveStepUp(index: number): void {
    if (index > 0) {
        swapArrayItems(form.steps, index - 1, index);
    }
}

function moveStepDown(index: number): void {
    if (index < form.steps.length - 1) {
        swapArrayItems(form.steps, index, index + 1);
    }
}

// 이미지 처리 함수
function onStepImageChange(e: Event, step: RecipeStepDraft): void {
    const input = e.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    
    step.file = file;
    step.previewUrl = handleImageFile(file, step.previewUrl);
}

function onThumbnailChange(e: Event): void {
    const input = e.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    
    form.thumbnailFile = file;
    form.thumbnailPreview = handleImageFile(file, form.thumbnailPreview);
}

function clearThumbnail(): void {
    clearImagePreview(form.thumbnailPreview);
    form.thumbnailPreview = '';
    form.thumbnailFile = null;
    if (thumbnailInputRef.value) {
        thumbnailInputRef.value.value = '';
    }
}

function clearStepImage(step: RecipeStepDraft): void {
    clearImagePreview(step.previewUrl);
    step.previewUrl = '';
    step.file = null;
    const inputRef = stepInputRefs.value[step.id];
    if (inputRef) {
        inputRef.value = '';
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
        .map((group) => ({
            type: group.type,
            items: group.items
                .filter((item) => item.name.trim() && item.quantity !== null && item.quantity > 0 && item.unit)
                .map((item, idx) => ({
                    order: idx + 1,
                    name: item.name.trim(),
                    quantity: item.quantity!,
                    unit: item.unit
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

async function saveAsDraft(): Promise<void> {
    submitting.value = true;
    try {
        const payload = buildRecipePayload('DRAFT');
        await httpJson(import.meta.env.VITE_API_BASE_URL_COOK, '/api/recipe/draft', {
            method: 'POST',
            body: JSON.stringify(payload)
        });

        toast.add({
            severity: 'success',
            summary: '저장 완료',
            detail: '초안이 저장되었습니다.',
            life: 3000
        });
    } catch (e) {
        console.error(e);
        toast.add({
            severity: 'error',
            summary: '저장 실패',
            detail: '초안 저장 중 오류가 발생했습니다.',
            life: 3000
        });
    } finally {
        submitting.value = false;
    }
}

async function submit(): Promise<void> {
    const validation = validateForm();
    if (!validation.valid) {
        const fieldName = validation.firstErrorFieldName || '필수 항목';
        
        toast.add({ 
            severity: 'error', 
            summary: '입력 오류', 
            detail: `${fieldName}을(를) 입력해주세요.`, 
            life: 3000 
        });
        if (validation.firstErrorField) {
            setTimeout(() => {
                focusFirstError(validation.firstErrorField!);
            }, 100);
        }
        return;
    }

    submitting.value = true;
    try {
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

        await httpForm(import.meta.env.VITE_API_BASE_URL_COOK, '/api/recipe', formData, { 
            method: 'POST' 
        });

        toast.add({
            severity: 'success',
            summary: '등록 완료',
            detail: '등록이 완료되었습니다.',
            life: 3000
        });

        router.push('/my/recipes');
    } catch (e) {
        console.error(e);
        toast.add({
            severity: 'error',
            summary: '등록 실패',
            detail: e instanceof Error ? e.message : '레시피 등록 중 오류가 발생했습니다.',
            life: 3000
        });
    } finally {
        submitting.value = false;
    }
}

function goBack(): void {
    router.push('/my/recipes');
}

// 초기화
onMounted(() => {
    loadCategoryOptions();
    loadCookingTipsOptions();
    loadMemberInfo();
});
</script>

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
</style>
