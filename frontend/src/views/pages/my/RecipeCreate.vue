<template>
    <div class="card">
        <div class="flex items-center justify-between mb-4">
            <h2 class="text-2xl font-bold">레시피 등록</h2>
            <div class="flex gap-2">
                <Button label="목록으로" icon="pi pi-arrow-left" severity="secondary" @click="goBack" :disabled="submitting" />
            </div>
        </div>

        <!-- 에러 메시지 표시 -->
        <Message v-if="error" severity="error" :closable="false" class="mb-4">
            {{ error }}
        </Message>

        <div class="flex flex-col gap-6">
            <!-- 첫 번째 영역: 대표 사진, 제목, 소개 -->
            <div class="border border-gray-200 rounded-lg p-5 bg-white">
                <h3 class="text-lg font-semibold mb-4 text-green-600">기본 정보</h3>
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

                    <!-- 제목 및 소개 (우측) -->
                    <div class="md:col-span-3 flex flex-col gap-4">
                        <div>
                            <label class="block mb-2 font-medium"><b>제목</b></label>
                            <InputText v-model.trim="form.title" placeholder="레시피 제목을 입력하세요" class="w-full" />
                        </div>
                        <div class="flex-1">
                            <label class="block mb-2 font-medium"><b>소개</b></label>
                            <Textarea v-model.trim="form.description" placeholder="간단한 소개나 메모를 작성하세요" class="w-full" :rows="9" />
                        </div>
                    </div>
                </div>
            </div>

            <!-- 두 번째 영역: 준비물 -->
            <div class="border border-gray-200 rounded-lg p-5 bg-white">
                <div class="flex items-center justify-between mb-4">
                    <h3 class="text-lg font-semibold text-green-600">준비물</h3>
                    <Button label="그룹 추가" icon="pi pi-plus" @click="addIngredientGroup" :disabled="submitting" />
                </div>
                <div v-if="form.ingredientGroups.length === 0" class="p-3 text-gray-500 border rounded">아직 준비물 그룹이 없습니다. '그룹 추가'를 눌러 시작하세요.</div>

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
                                />
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 세 번째 영역: 카테고리, 요리팁 -->
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
                                <div v-for="option in categoryOptions" :key="option.codeId" class="flex flex-col gap-2">
                                    <span class="text-sm font-medium text-gray-700">{{ option.codeName }}</span>
                                    <Select 
                                        v-model="form.categories[option.codeId]" 
                                        :options="getCategoryDetailOptions(option)" 
                                        optionLabel="label" 
                                        optionValue="value"
                                        placeholder="선택하세요"
                                        class="w-full"
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
                                <div v-for="option in cookingTipsOptions" :key="option.codeId" class="flex flex-col gap-2">
                                    <span class="text-sm font-medium text-gray-700">{{ option.codeName }}</span>
                                    <Select 
                                        v-model="form.cookingTips[option.codeId]" 
                                        :options="getCookingTipDetailOptions(option)" 
                                        optionLabel="label" 
                                        optionValue="value"
                                        placeholder="선택하세요"
                                        class="w-full"
                                    />
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 네 번째 영역: 조리 순서 -->
            <div class="border border-gray-200 rounded-lg p-5 bg-white">
                <div class="flex items-center justify-between mb-4">
                    <h3 class="text-lg font-semibold text-green-600">조리 순서</h3>
                    <Button label="단계 추가" icon="pi pi-plus" @click="addStep" :disabled="submitting" />
                </div>
                <div v-if="form.steps.length === 0" class="p-3 text-gray-500 border rounded">아직 단계가 없습니다. '단계 추가'를 눌러 시작하세요.</div>

                <div v-for="(step, index) in form.steps" :key="step.id" class="border rounded p-3 mb-3 bg-gray-50">
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
                            <Textarea v-model.trim="step.text" placeholder="이 단계에서의 설명을 작성하세요" class="w-full" :rows="9"  />
                        </div>
                    </div>
                </div>
            </div>

            <!-- 다섯 번째 영역: 공개 여부, 상태, 저장 버튼 -->
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
                    <Button label="등록" icon="pi pi-check" severity="success" @click="submit" :disabled="submitting || !isValid" />
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { httpForm, httpJson } from '@/utils/http';
import { fetchMemberInfo } from '@/utils/auth';
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import Button from 'primevue/button';
import InputNumber from 'primevue/inputnumber';
import InputText from 'primevue/inputtext';
import Message from 'primevue/message';
import Select from 'primevue/select';
import Textarea from 'primevue/textarea';

const router = useRouter();

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

const visibilityOptions = [
    { label: '공개', value: 'PUBLIC' },
    { label: '비공개', value: 'PRIVATE' }
];

const statusOptions = [
    { label: '초안', value: 'DRAFT' },
    { label: '발행', value: 'PUBLISHED' }
];

// 더미 주제 타입 데이터 (추후 공통코드로 대체 예정)
const ingredientTypeOptions = [
    { label: '재료', value: 'INGREDIENT' },
    { label: '양념', value: 'SEASONING' },
    { label: '조리도구', value: 'TOOL' },
    { label: '기타', value: 'OTHER' }
];

// 더미 단위 데이터 (추후 공통코드로 대체 예정)
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

const isValid = computed(() => {
    const basicValid = Boolean(form.title.trim());
    const stepsValid = form.steps.length > 0 && form.steps.every((s) => s.text.trim());
    const categoriesValid = categoryOptions.value.length === 0 || categoryOptions.value.every((option) => !!form.categories[option.codeId]);
    const cookingTipsValid = cookingTipsOptions.value.length === 0 || cookingTipsOptions.value.every((option) => !!form.cookingTips[option.codeId]);
    return basicValid && stepsValid && categoriesValid && cookingTipsValid;
});

onMounted(() => {
    loadCategoryOptions();
    loadCookingTipsOptions();
    loadMemberInfo();
});

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

async function loadMemberInfo() {
    const memberInfo = await fetchMemberInfo();
    if (memberInfo) {
        form.memberId = memberInfo.id;
    }
}

function addIngredientGroup() {
    form.ingredientGroups.push({ 
        id: crypto.randomUUID(), 
        type: '',
        items: []
    });
}

function removeIngredientGroup(groupIndex: number) {
    form.ingredientGroups.splice(groupIndex, 1);
}

function moveIngredientGroupUp(groupIndex: number) {
    if (groupIndex <= 0) return;
    const tmp = form.ingredientGroups[groupIndex - 1];
    form.ingredientGroups[groupIndex - 1] = form.ingredientGroups[groupIndex];
    form.ingredientGroups[groupIndex] = tmp;
}

function moveIngredientGroupDown(groupIndex: number) {
    if (groupIndex >= form.ingredientGroups.length - 1) return;
    const tmp = form.ingredientGroups[groupIndex + 1];
    form.ingredientGroups[groupIndex + 1] = form.ingredientGroups[groupIndex];
    form.ingredientGroups[groupIndex] = tmp;
}

function addIngredientItem(groupIndex: number) {
    const group = form.ingredientGroups[groupIndex];
    if (group) {
        group.items.push({ 
            id: crypto.randomUUID(), 
            name: '', 
            quantity: null, 
            unit: '' 
        });
    }
}

function removeIngredientItem(groupIndex: number, itemIndex: number) {
    const group = form.ingredientGroups[groupIndex];
    if (group) {
        group.items.splice(itemIndex, 1);
    }
}

function moveIngredientItemUp(groupIndex: number, itemIndex: number) {
    const group = form.ingredientGroups[groupIndex];
    if (!group || itemIndex <= 0) return;
    const tmp = group.items[itemIndex - 1];
    group.items[itemIndex - 1] = group.items[itemIndex];
    group.items[itemIndex] = tmp;
}

function moveIngredientItemDown(groupIndex: number, itemIndex: number) {
    const group = form.ingredientGroups[groupIndex];
    if (!group || itemIndex >= group.items.length - 1) return;
    const tmp = group.items[itemIndex + 1];
    group.items[itemIndex + 1] = group.items[itemIndex];
    group.items[itemIndex] = tmp;
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
    if (thumbnailInputRef.value) {
        thumbnailInputRef.value.value = '';
    }
}

function clearStepImage(step: RecipeStepDraft) {
    if (step.previewUrl) URL.revokeObjectURL(step.previewUrl);
    step.previewUrl = '';
    step.file = null;
    const inputRef = stepInputRefs.value[step.id];
    if (inputRef) {
        inputRef.value = '';
    }
}

function goBack() {
    router.push('/my/recipes');
}

function getCategoryDetailOptions(option: CommonCodeOption) {
    return [
        { label: '선택하세요', value: '' },
        ...option.details.map(detail => ({
            label: detail.codeName,
            value: detail.detailCodeId
        }))
    ];
}

function getCookingTipDetailOptions(option: CommonCodeOption) {
    return [
        { label: '선택하세요', value: '' },
        ...option.details.map(detail => ({
            label: detail.codeName,
            value: detail.detailCodeId
        }))
    ];
}

function buildRecipePayload(statusOverride?: 'DRAFT' | 'PUBLISHED') {
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
        // HttpOnly 쿠키를 통해 인증이 자동으로 처리되므로 토큰 검증 불필요

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

<style scoped>
:deep(.p-textarea) {
    resize: none;
    overflow-y: auto;
}
</style>

