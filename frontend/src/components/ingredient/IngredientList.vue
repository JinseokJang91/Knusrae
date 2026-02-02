<template>
    <div class="ingredient-list">
        <!-- 검색 바 + 재료 정보 요청 버튼 -->
        <div class="search-row mb-6">
            <span class="p-input-icon-left search-bar">
                <InputText 
                    v-model="localSearchQuery" 
                    placeholder="재료명을 검색하세요...(예: 감자, 계란)" 
                    class="w-full"
                    @input="handleSearchInput"
                />
            </span>
            <Button 
                label="재료 정보 요청하기" 
                icon="pi pi-send"
                severity="secondary"
                outlined
                class="request-btn"
                @click="openRequestDialog"
            />
        </div>

        <!-- 재료 그룹 선택 -->
        <IngredientGroupSelector
            :groups="groups"
            :selected-group-id="selectedGroupId"
            @select="handleGroupSelect"
            class="mb-6"
        />

        <!-- 그룹 선택 ↔ 재료 목록 구분 -->
        <div class="list-section-divider" aria-hidden="true"></div>

        <!-- 로딩 상태 -->
        <div v-if="loading" class="text-center py-8">
            <ProgressSpinner />
            <p class="text-gray-600 mt-3">재료를 불러오는 중...</p>
        </div>

        <!-- 에러 상태 -->
        <div v-else-if="error" class="text-center py-8">
            <i class="pi pi-exclamation-triangle text-6xl text-red-500 mb-4"></i>
            <h3 class="text-2xl font-semibold text-gray-600 mb-2">재료를 불러올 수 없습니다</h3>
            <p class="text-gray-600 mb-4">{{ error }}</p>
            <Button label="다시 시도" @click="loadIngredients" />
        </div>

        <!-- 재료 목록 -->
        <div v-else-if="ingredients.length > 0">
            <IngredientGrid
                :ingredients="ingredients"
                :type="type"
                @ingredient-click="handleIngredientClick"
            />
        </div>

        <!-- 빈 상태 -->
        <div v-else class="text-center py-12">
            <i class="pi pi-inbox text-6xl text-gray-400 mb-4"></i>
            <h3 class="text-xl font-semibold text-gray-600 mb-2">재료가 없습니다</h3>
            <p class="text-gray-500">검색 조건을 변경해보세요.</p>
        </div>

        <!-- 상세 정보 다이얼로그 -->
        <IngredientDetailDialog
            :visible="detailDialogVisible"
            :ingredient="selectedIngredient"
            :type="type"
            @close="handleDetailClose"
        />

        <!-- 재료 정보 요청 다이얼로그 -->
        <Dialog
            v-model:visible="showRequestDialog"
            header="재료 정보 요청"
            :modal="true"
            :style="{ width: '90vw', maxWidth: '500px' }"
        >
            <div class="request-form">
                <div class="mb-4">
                    <label class="block mb-2 font-semibold">재료명</label>
                    <InputText 
                        v-model="requestForm.ingredientName" 
                        class="w-full" 
                        placeholder="요청할 재료명을 입력하세요"
                    />
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
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import InputText from 'primevue/inputtext';
import Button from 'primevue/button';
import Dialog from 'primevue/dialog';
import Select from 'primevue/select';
import Textarea from 'primevue/textarea';
import ProgressSpinner from 'primevue/progressspinner';
import { useToast } from 'primevue/usetoast';
import { getIngredientGroups, getIngredients, createIngredientRequest } from '@/api/ingredientApi';
import type { IngredientGroup, Ingredient, IngredientType } from '@/types/ingredient';
import IngredientGroupSelector from './IngredientGroupSelector.vue';
import IngredientGrid from './IngredientGrid.vue';
import IngredientDetailDialog from './IngredientDetailDialog.vue';

const props = defineProps<{
    type: IngredientType;
    selectedGroupId?: number | null;
    searchQuery?: string;
}>();

const emit = defineEmits<{
    (e: 'group-selected', groupId: number | null): void;
    (e: 'search-changed', query: string): void;
}>();

const groups = ref<IngredientGroup[]>([]);
const ingredients = ref<Ingredient[]>([]);
const loading = ref(false);
const error = ref<string | null>(null);
const localSearchQuery = ref(props.searchQuery || '');
const selectedGroupId = ref<number | null>(props.selectedGroupId || null);
const detailDialogVisible = ref(false);
const selectedIngredient = ref<Ingredient | null>(null);
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

const toast = useToast();

// 검색 디바운스 (300ms)
let searchTimeout: ReturnType<typeof setTimeout> | null = null;

const handleSearchInput = () => {
    if (searchTimeout) {
        clearTimeout(searchTimeout);
    }
    
    searchTimeout = setTimeout(() => {
        emit('search-changed', localSearchQuery.value);
        loadIngredients();
    }, 300);
};

const handleGroupSelect = (groupId: number | null) => {
    selectedGroupId.value = groupId;
    emit('group-selected', groupId);
    loadIngredients();
};

const handleIngredientClick = (ingredient: Ingredient) => {
    selectedIngredient.value = ingredient;
    detailDialogVisible.value = true;
};

const handleDetailClose = () => {
    detailDialogVisible.value = false;
    selectedIngredient.value = null;
};

const openRequestDialog = () => {
    requestForm.value.requestType = props.type === 'storage' ? 'STORAGE' : 'PREPARATION';
    showRequestDialog.value = true;
};

const handleRequestSubmit = async () => {
    if (!requestForm.value.ingredientName.trim()) {
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
            ingredientName: requestForm.value.ingredientName.trim(),
            requestType: requestForm.value.requestType,
            message: requestForm.value.message?.trim() || undefined
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
            requestType: props.type === 'storage' ? 'STORAGE' : 'PREPARATION',
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

const loadGroups = async () => {
    try {
        groups.value = await getIngredientGroups();
    } catch (err) {
        console.error('재료 그룹 로딩 실패:', err);
        error.value = '재료 그룹을 불러올 수 없습니다.';
    }
};

const loadIngredients = async () => {
    loading.value = true;
    error.value = null;

    try {
        const result = await getIngredients({
            groupId: selectedGroupId.value || undefined,
            searchQuery: localSearchQuery.value || undefined,
            type: props.type,
            limit: 100,
            offset: 0
        });

        ingredients.value = result.ingredients;

        // 그룹 목록이 비어있으면 로드
        if (groups.value.length === 0) {
            groups.value = result.groups;
        }
    } catch (err: any) {
        console.error('재료 목록 로딩 실패:', err);
        error.value = err.message || '재료를 불러올 수 없습니다.';
    } finally {
        loading.value = false;
    }
};

// props 변경 감지 (탭/그룹/검색 변경 시 재조회)
watch(() => props.selectedGroupId, (newValue) => {
    selectedGroupId.value = newValue ?? null;
    loadIngredients();
});

watch(() => props.searchQuery, (newValue) => {
    localSearchQuery.value = newValue || '';
    loadIngredients();
});

watch(() => props.type, () => {
    loadIngredients();
});

onMounted(() => {
    loadGroups();
    loadIngredients();
});
</script>

<style scoped>
.ingredient-list {
    min-height: 400px;
}

.search-row {
    display: flex;
    align-items: center;
    gap: 1rem;
}

.search-bar {
    flex: 0 1 600px;
    min-width: 0;
}

/* RecipeDetail 댓글 이미지 첨부 버튼과 동일: bg-gray-100, hover:bg-gray-200, text-gray-700 */
.request-btn {
    flex-shrink: 0;
    margin-left: auto;
    background-color: #f3f4f6 !important;
    color: #374151 !important;
    border-color: #e5e7eb !important;
}

.request-btn:hover {
    background-color: #e5e7eb !important;
    border-color: #d1d5db !important;
}

/* 재료 그룹 선택 ↔ 재료 목록 구분선 */
.list-section-divider {
    height: 1px;
    margin: 1.5rem 0 1.25rem;
    background: var(--surface-border);
}
</style>
