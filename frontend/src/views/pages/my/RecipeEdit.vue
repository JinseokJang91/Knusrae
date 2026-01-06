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
            <div class="mb-6 p-4 bg-green-50 border-l-4 border-green-500 rounded-r">
                <p class="text-gray-700 italic">
                    셰프님이 누군가를 위해 정성들인 이 요리처럼, 레시피에서도 셰프님의 따뜻한 정성을 보여주세요.
                </p>
            </div>

            <!-- 기본 정보: 대표 사진, 제목, 소개 -->
            <div class="border border-gray-200 rounded-lg p-5 bg-white">
                <div class="flex items-center gap-1 mb-1">
                    <h3 class="text-xl font-semibold text-green-600">
                        <span class="mr-1">기본 정보</span>
                        <i 
                            ref="el => { if (el) guideIconRefs.basic = el as HTMLElement; }"
                            class="pi pi-question-circle help-button" 
                            @click="showGuide('basic', $event)" 
                            style="cursor: pointer;"
                        />
                        <Popover 
                            :ref="el => { if (el) guidePopoverRefs.basic = el; }"
                            :target="guideIconRefs.basic"
                            :showCloseIcon="true"
                            :dismissable="true"
                        >
                            <div class="p-2">
                                <img :src="guideImages.basic" alt="기본 정보 가이드" class="max-w-full h-auto" />
                            </div>
                        </Popover>
                    </h3>
                </div>
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
                            <div v-else class="group relative w-full h-full">
                                <img :src="form.thumbnailPreview" alt="thumbnail preview" class="w-full h-full object-cover rounded-md" />
                                <button 
                                    class="absolute top-2 right-2 w-8 h-8 bg-red-500 text-white rounded-full flex items-center justify-center hover:bg-red-600 shadow-lg opacity-0 group-hover:opacity-100 transition-opacity duration-200"
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
                            <InputText v-model.trim="form.title" placeholder="레시피 제목을 입력하세요" class="w-full" />
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
                    <div class="flex items-center gap-2">
                        <h3 class="text-xl font-semibold text-green-600">
                            <span class="mr-1">준비물</span>
                            <i 
                                ref="el => { if (el) guideIconRefs.ingredients = el as HTMLElement; }"
                                class="pi pi-question-circle help-button" 
                                @click="showGuide('ingredients', $event)" 
                                style="cursor: pointer;"
                            />
                            <Popover 
                                :ref="el => { if (el) guidePopoverRefs.ingredients = el; }"
                                :target="guideIconRefs.ingredients"
                                :showCloseIcon="true"
                                :dismissable="true"
                            >
                                <div class="p-2">
                                    <img :src="guideImages.ingredients" alt="준비물 가이드" class="max-w-full h-auto" />
                                </div>
                            </Popover>
                        </h3>
                    </div>
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
                                <Message v-if="ingredientTypesError" severity="error" :closable="false" class="mb-2">
                                    {{ ingredientTypesError }}
                                </Message>
                                <div v-else-if="ingredientTypesLoading" class="p-2 text-sm text-gray-500">
                                    로딩 중...
                                </div>
                                <Select v-else
                                    v-model="group.type" 
                                    :options="getIngredientTypeOptions()" 
                                    optionLabel="label" 
                                    optionValue="value"
                                    placeholder="주제를 선택하세요"
                                    class="w-full"
                                    filter
                                    showClear
                                />
                            </div>
                            <!-- 직접 입력 필드 (type이 'CUSTOM'일 때만 표시) -->
                            <div v-if="group.type === 'CUSTOM'" class="flex-1 max-w-xs">
                                <InputText 
                                    v-model.trim="group.customTypeName"
                                    placeholder="그룹 타입을 직접 입력하세요"
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
                                <label class="block mb-2 text-sm">수량 <span class="text-gray-400 text-xs">(선택사항, 분수 입력 가능: 1/2, 3/4 등)</span></label>
                                <InputText v-model.trim="item.quantity" placeholder="수량을 입력하세요 (예: 1, 1.5, 1/2)" class="w-full" />
                            </div>
                            <div>
                                <label class="block mb-2 text-sm">단위 <span class="text-gray-400 text-xs">(선택사항)</span></label>
                                <Message v-if="unitsError" severity="error" :closable="false" class="mb-2 text-xs">
                                    {{ unitsError }}
                                </Message>
                                <div v-else-if="unitsLoading" class="p-2 text-xs text-gray-500">
                                    로딩 중...
                                </div>
                                <div v-else class="flex flex-col gap-2">
                                    <Select
                                        v-model="item.unit" 
                                        :options="getUnitOptions()" 
                                        optionLabel="label" 
                                        optionValue="value"
                                        placeholder="단위를 선택하세요 (선택사항)"
                                        class="w-full"
                                        filter
                                        showClear
                                    />
                                    <!-- 직접 입력 필드 (unit이 'CUSTOM'일 때만 표시) -->
                                    <InputText 
                                        v-if="item.unit === 'CUSTOM'"
                                        v-model.trim="item.customUnitName"
                                        placeholder="단위를 직접 입력하세요"
                                        class="w-full"
                                    />
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 분류 정보: 카테고리, 요리팁 -->
            <div class="border border-gray-200 rounded-lg p-5 bg-white">
                <div class="flex items-center gap-1 mb-1">
                    <h3 class="text-xl font-semibold text-green-600">
                        <span class="mr-1">분류 정보</span>
                        <i 
                            ref="el => { if (el) guideIconRefs.classification = el as HTMLElement; }"
                            class="pi pi-question-circle help-button" 
                            @click="showGuide('classification', $event)" 
                            style="cursor: pointer;"
                        />
                        <Popover 
                            :ref="el => { if (el) guidePopoverRefs.classification = el; }"
                            :target="guideIconRefs.classification"
                            :showCloseIcon="true"
                            :dismissable="true"
                        >
                            <div class="p-2">
                                <img :src="guideImages.classification" alt="분류 정보 가이드" class="max-w-full h-auto" />
                            </div>
                        </Popover>
                    </h3>
                </div>
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
                                        :options="getCommonCodeDetailOptions(option)" 
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
                                        :options="getCommonCodeDetailOptions(option)" 
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

            <!-- 조리 순서 -->
            <div class="border border-gray-200 rounded-lg p-5 bg-white">
                <div class="flex items-center justify-between mb-4">
                    <div class="flex items-center gap-2">
                        <h3 class="text-xl font-semibold text-green-600">
                            <span class="mr-1">조리 순서</span>
                            <i 
                                ref="el => { if (el) guideIconRefs.steps = el as HTMLElement; }"
                                class="pi pi-question-circle help-button" 
                                @click="showGuide('steps', $event)" 
                                style="cursor: pointer;"
                            />
                            <Popover 
                                :ref="el => { if (el) guidePopoverRefs.steps = el; }"
                                :target="guideIconRefs.steps"
                                :showCloseIcon="true"
                                :dismissable="true"
                            >
                                <div class="p-2">
                                    <img :src="guideImages.steps" alt="조리 순서 가이드" class="max-w-full h-auto" />
                                </div>
                            </Popover>
                        </h3>
                    </div>
                    <Button label="단계 추가" icon="pi pi-plus" @click="addStep" :disabled="submitting" />
                </div>
                <div v-if="form.steps.length === 0" class="p-3 text-gray-500 border rounded">
                    아직 단계가 없습니다. '단계 추가'를 눌러 시작하세요.
                </div>

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
                                <div v-else class="group relative w-full h-full">
                                    <img :src="step.previewUrl" alt="step preview" class="w-full h-full object-cover rounded-md" />
                                    <button 
                                        class="absolute top-2 right-2 w-8 h-8 bg-red-500 text-white rounded-full flex items-center justify-center hover:bg-red-600 shadow-lg opacity-0 group-hover:opacity-100 transition-opacity duration-200"
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
                            <Textarea 
                                v-model.trim="step.text" 
                                placeholder="이 단계에서의 설명을 작성하세요" 
                                class="w-full" 
                                :rows="9"
                            />
                        </div>
                    </div>
                </div>
            </div>

            <!-- 설정 및 저장 -->
            <div class="border border-gray-200 rounded-lg p-5 bg-white">
                <div class="flex items-center gap-1 mb-1">
                    <h3 class="text-xl font-semibold text-green-600">
                        <span class="mr-1">설정 및 저장</span>
                        <i 
                            ref="el => { if (el) guideIconRefs.settings = el as HTMLElement; }"
                            class="pi pi-question-circle help-button" 
                            @click="showGuide('settings', $event)" 
                            style="cursor: pointer;"
                        />
                        <Popover 
                            :ref="el => { if (el) guidePopoverRefs.settings = el; }"
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
                    <Button label="취소" icon="pi pi-times" severity="secondary" @click="goBack" :disabled="submitting" />
                    <Button label="수정" icon="pi pi-check" severity="success" @click="submit" :disabled="submitting || !isValid" />
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { httpForm, httpJson } from '@/utils/http';
import { GUIDE_IMAGES, getApiBaseUrl } from '@/utils/constants';
import { useErrorHandler } from '@/utils/errorHandler';
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue';
import { onBeforeRouteLeave, useRoute, useRouter } from 'vue-router';
import { useConfirm } from 'primevue/useconfirm';
import Button from 'primevue/button';
import InputText from 'primevue/inputtext';
import Message from 'primevue/message';
import Popover from 'primevue/popover';
import Select from 'primevue/select';
import Textarea from 'primevue/textarea';

const router = useRouter();
const route = useRoute();
const recipeId = computed(() => Number(route.params.id));
const confirm = useConfirm();
const { handleApiCallVoid } = useErrorHandler();

interface RecipeStepDraft {
    id: string;
    file?: File | null;
    previewUrl?: string;
    text: string;
    existingImageUrl?: string; // 기존 이미지 URL
}

interface IngredientItemDraft {
    id: string;
    name: string;
    quantity: string | null; // 수량 (분수 입력 지원: "1/2", "3/4", "1.5" 등)
    unit: string;
    customUnitName?: string;  // 직접 입력 단위
}

interface IngredientGroupDraft {
    id: string;
    type: string;
    customTypeName?: string;  // 직접 입력 타입
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
const thumbnailInputRef = ref<HTMLInputElement | null>(null);
const stepInputRefs = ref<Record<string, HTMLInputElement>>({});
const hasUnsavedChanges = ref(false);
const isSubmitSuccessful = ref(false);
const originalFormData = ref<string>('');
const guideIconRefs = ref<Record<string, HTMLElement | null>>({});
const guidePopoverRefs = ref<Record<string, any>>({});

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
    const categoriesValid = categoryOptions.value.length === 0 || categoryOptions.value.every((option) => !!form.categories[option.codeId]);
    const cookingTipsValid = cookingTipsOptions.value.length === 0 || cookingTipsOptions.value.every((option) => !!form.cookingTips[option.codeId]);
    return basicValid && stepsValid && categoriesValid && cookingTipsValid;
});

onMounted(() => {
    const initializeRecipeEdit = async () => {
        await Promise.all([
            loadCategoryOptions(),
            loadCookingTipsOptions(),
            loadIngredientsGroupOptions(),
            loadIngredientsUnitOptions(),
            loadRecipeData()
        ]);
        initialLoading.value = false;
    };
    initializeRecipeEdit();
    
    // beforeunload 이벤트 리스너 등록
    window.addEventListener('beforeunload', handleBeforeUnload);
});

async function loadRecipeData() {
    try {
        const API_COOK_BASE_URL = getApiBaseUrl('cook');
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

        // 준비물 설정
        if (response.ingredientGroups && Array.isArray(response.ingredientGroups)) {
            form.ingredientGroups = response.ingredientGroups.map((group: any) => ({
                id: crypto.randomUUID(),
                type: group.detailCodeId || (group.customTypeName ? 'CUSTOM' : ''),
                customTypeName: group.customTypeName || '',
                items: Array.isArray(group.items) ? group.items.map((item: any) => ({
                    id: crypto.randomUUID(),
                    name: item.name || '',
                    quantity: item.quantity || null, // String으로 직접 저장 (분수 입력 지원)
                    unit: item.detailCodeId || (item.customUnitName ? 'CUSTOM' : ''),
                    customUnitName: item.customUnitName || ''
                })) : []
            }));
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

        // 원본 데이터 저장 (변경 감지용)
        originalFormData.value = JSON.stringify({
            title: form.title,
            description: form.description,
            status: form.status,
            visibility: form.visibility,
            thumbnailPreview: form.thumbnailPreview,
            steps: form.steps.map(s => ({ text: s.text, existingImageUrl: s.existingImageUrl })),
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
        const response = await httpJson(getApiBaseUrl('cook'), '/api/common-codes?codeGroup=CATEGORY', {
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
        const response = await httpJson(getApiBaseUrl('cook'), '/api/common-codes?codeGroup=COOKINGTIP', {
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

async function loadIngredientsGroupOptions() {
    ingredientTypesLoading.value = true;
    ingredientTypesError.value = null;
    try {
        const response = await httpJson(
            getApiBaseUrl('cook'),
            '/api/common-codes?codeId=INGREDIENTS_GROUP',
            { method: 'GET' }
        );
        ingredientTypeOptions.value = Array.isArray(response) ? response : [];
    } catch (e) {
        ingredientTypesError.value = '재료 타입 정보를 불러오지 못했습니다.';
    } finally {
        ingredientTypesLoading.value = false;
    }
}

async function loadIngredientsUnitOptions() {
    unitsLoading.value = true;
    unitsError.value = null;
    try {
        const response = await httpJson(
            getApiBaseUrl('cook'),
            '/api/common-codes?codeId=INGREDIENTS_UNIT',
            { method: 'GET' }
        );
        unitOptions.value = Array.isArray(response) ? response : [];
    } catch (e) {
        unitsError.value = '단위 정보를 불러오지 못했습니다.';
    } finally {
        unitsLoading.value = false;
    }
}

function getIngredientTypeOptions() {
    if (!ingredientTypeOptions.value || ingredientTypeOptions.value.length === 0) {
        return [{ label: '직접 입력', value: 'CUSTOM' }];
    }
    const options = ingredientTypeOptions.value.flatMap(option => 
        option.details.map(detail => ({
            label: detail.codeName,
            value: detail.detailCodeId
        }))
    );
    // 직접 입력 옵션 추가
    return [...options, { label: '직접 입력', value: 'CUSTOM' }];
}

function getUnitOptions() {
    if (!unitOptions.value || unitOptions.value.length === 0) {
        return [{ label: '직접 입력', value: 'CUSTOM' }];
    }
    const options = unitOptions.value.flatMap(option => 
        option.details.map(detail => ({
            label: detail.codeName,
            value: detail.detailCodeId
        }))
    );
    // 직접 입력 옵션 추가
    return [...options, { label: '직접 입력', value: 'CUSTOM' }];
}

function getCommonCodeDetailOptions(option: CommonCodeOption) {
    return [
        { label: '선택하세요', value: '' },
        ...option.details.map(detail => ({
            label: detail.codeName,
            value: detail.detailCodeId
        }))
    ];
}

const visibilityOptions = [
    { label: '공개', value: 'PUBLIC' },
    { label: '비공개', value: 'PRIVATE' }
];

const statusOptions = [
    { label: '초안', value: 'DRAFT' },
    { label: '발행', value: 'PUBLISHED' }
];

function swapArrayItems<T>(array: T[], index1: number, index2: number): void {
    if (index1 < 0 || index2 < 0 || index1 >= array.length || index2 >= array.length) return;
    [array[index1], array[index2]] = [array[index2], array[index1]];
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
    if (groupIndex > 0) {
        swapArrayItems(form.ingredientGroups, groupIndex - 1, groupIndex);
    }
}

function moveIngredientGroupDown(groupIndex: number) {
    if (groupIndex < form.ingredientGroups.length - 1) {
        swapArrayItems(form.ingredientGroups, groupIndex, groupIndex + 1);
    }
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
    if (group && itemIndex > 0) {
        swapArrayItems(group.items, itemIndex - 1, itemIndex);
    }
}

function moveIngredientItemDown(groupIndex: number, itemIndex: number) {
    const group = form.ingredientGroups[groupIndex];
    if (group && itemIndex < group.items.length - 1) {
        swapArrayItems(group.items, itemIndex, itemIndex + 1);
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
        const success = await handleApiCallVoid(
            () => httpForm(getApiBaseUrl('cook'), `/api/recipe/${recipeId.value}`, formData, { method: 'PUT' }),
            '레시피 수정 중 오류가 발생했습니다.',
            '수정 실패'
        );

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
    () => [
        form.title,
        form.description,
        form.status,
        form.visibility,
        form.thumbnailFile,
        form.steps.length,
        form.ingredientGroups.length,
        JSON.stringify(form.categories),
        JSON.stringify(form.cookingTips)
    ],
    () => {
        // 초기 로딩이 완료되고 원본 데이터가 있을 때만 비교
        if (originalFormData.value && !initialLoading.value) {
            const original = JSON.parse(originalFormData.value);
            
            // 파일 변경 감지
            const hasFileChange = Boolean(form.thumbnailFile) || form.steps.some(s => s.file);
            
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
    color: #16a34a;
    opacity: 0.7;
    transition: opacity 0.2s;
}

.help-button:hover {
    opacity: 1;
}
</style>

