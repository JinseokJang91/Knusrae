<template>
    <div class="ingredient-list">
        <!-- 검색 바 -->
        <div class="search-bar mb-6">
            <span class="p-input-icon-left w-full">
                <i class="pi pi-search" />
                <InputText 
                    v-model="localSearchQuery" 
                    placeholder="재료명을 검색하세요..." 
                    class="w-full"
                    @input="handleSearchInput"
                />
            </span>
        </div>

        <!-- 재료 그룹 선택 -->
        <IngredientGroupSelector
            :groups="groups"
            :selected-group-id="selectedGroupId"
            @select="handleGroupSelect"
            class="mb-6"
        />

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
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import InputText from 'primevue/inputtext';
import Button from 'primevue/button';
import ProgressSpinner from 'primevue/progressspinner';
import { getIngredientGroups, getIngredients } from '@/api/ingredientApi';
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

// props 변경 감지
watch(() => props.selectedGroupId, (newValue) => {
    selectedGroupId.value = newValue ?? null;
    loadIngredients();
});

watch(() => props.searchQuery, (newValue) => {
    localSearchQuery.value = newValue || '';
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

.search-bar {
    max-width: 600px;
}
</style>
