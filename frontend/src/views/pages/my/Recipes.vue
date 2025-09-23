<template>
    <div class="card">
        <div class="flex items-center justify-between mb-4">
            <h2 class="text-2xl font-bold">레시피 관리</h2>
            <div class="flex gap-2">
                <input v-model="search" type="text" class="p-inputtext p-component" placeholder="레시피 검색" />
                <button @click="handleCreateRecipe" class="p-button p-component" :disabled="loading">
                    <span class="pi pi-plus mr-2"></span>
                    <span>레시피 등록하기</span>
                </button>
            </div>
        </div>

        <!-- 에러 메시지 표시 -->
        <div v-if="error" class="mb-4 p-3 bg-red-100 border border-red-400 text-red-700 rounded">
            {{ error }}
        </div>

        <!-- 로딩 상태 표시 -->
        <div v-if="loading" class="flex justify-center items-center py-8">
            <div class="pi pi-spinner pi-spin mr-2"></div>
            <span>로딩 중...</span>
        </div>

        <!-- 레시피 테이블 -->
        <div v-else class="overflow-auto border rounded-md">
            <table class="w-full text-sm">
                <thead>
                    <tr class="bg-surface-100 text-left">
                        <th class="px-3 py-2 w-16">#</th>
                        <th class="px-3 py-2">제목</th>
                        <th class="px-3 py-2 w-28">상태</th>
                        <th class="px-3 py-2 w-28">공개</th>
                        <th class="px-3 py-2 w-40 text-right">액션</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-if="filteredRecipes.length === 0">
                        <td colspan="5" class="px-3 py-8 text-center text-surface-500">등록된 레시피가 없습니다.</td>
                    </tr>
                    <tr v-for="(r, i) in filteredRecipes" :key="r.id" class="border-t">
                        <td class="px-3 py-2">{{ i + 1 }}</td>
                        <td class="px-3 py-2">{{ r.title }}</td>
                        <td class="px-3 py-2">
                            <span class="px-2 py-1 rounded text-xs" :class="r.status === 'draft' ? 'bg-yellow-100 text-yellow-700' : 'bg-green-100 text-green-700'">
                                {{ r.status === 'draft' ? '초안' : '발행' }}
                            </span>
                        </td>
                        <td class="px-3 py-2">
                            <span class="px-2 py-1 rounded text-xs" :class="r.visibility === 'public' ? 'bg-blue-100 text-blue-700' : 'bg-gray-100 text-gray-700'">
                                {{ r.visibility === 'public' ? '공개' : '비공개' }}
                            </span>
                        </td>
                        <td class="px-3 py-2 text-right">
                            <button @click="handleEditRecipe(r)" class="p-button p-component p-button-text" :disabled="loading">
                                <span class="pi pi-pencil"></span>
                            </button>
                            <button @click="handleDeleteRecipe(r.id)" class="p-button p-component p-button-text text-red-600" :disabled="loading">
                                <span class="pi pi-trash"></span>
                            </button>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';

// API 호출을 위한 기본 URL
const API_COOK_BASE_URL = import.meta.env.VITE_API_BASE_URL_COOK;

// 레시피 타입 정의
interface Recipe {
    id: number;
    title: string;
    status: 'draft' | 'published';
    visibility: 'public' | 'private';
    description?: string;
    ingredients?: string[];
    instructions?: string[];
    createdAt?: string;
    updatedAt?: string;
}

// 레시피 생성/수정을 위한 타입 (향후 사용 예정)
// interface RecipeCreateRequest {
//     title: string;
//     description?: string;
//     status: 'draft' | 'published';
//     visibility: 'public' | 'private';
//     ingredients?: string[];
//     instructions?: string[];
// }

// API 호출 함수들
const apiCall = async (url: string, options: RequestInit = {}) => {
    try {
        const response = await fetch(`${API_COOK_BASE_URL}${url}`, {
            headers: {
                'Content-Type': 'application/json',
                ...options.headers
            },
            ...options
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        return await response.json();
    } catch (error) {
        console.error('API 호출 중 오류 발생:', error);
        throw error;
    }
};

// 1. 레시피 목록 조회
const fetchRecipes = async (): Promise<Recipe[]> => {
    return await apiCall('/api/recipe/list');
};

// 2. 레시피 등록 (향후 사용 예정)
// const createRecipe = async (recipeData: RecipeCreateRequest): Promise<Recipe> => {
//     return await apiCall('/api/recipe', {
//         method: 'POST',
//         body: JSON.stringify(recipeData),
//     });
// };

// 3. 레시피 수정 (향후 사용 예정)
// const updateRecipe = async (id: number, recipeData: RecipeCreateRequest): Promise<Recipe> => {
//     return await apiCall(`/api/recipe/${id}`, {
//         method: 'PUT',
//         body: JSON.stringify(recipeData),
//     });
// };

// 4. 레시피 삭제
const deleteRecipe = async (id: number): Promise<void> => {
    await apiCall(`/api/recipe/${id}`, {
        method: 'DELETE'
    });
};

// 컴포넌트 상태
const search = ref('');
const rows = ref<Recipe[]>([]);
const loading = ref(false);
const error = ref<string | null>(null);

// 레시피 목록 로드
const loadRecipes = async () => {
    loading.value = true;
    error.value = null;
    try {
        rows.value = await fetchRecipes();
    } catch (err) {
        error.value = '레시피 목록을 불러오는 중 오류가 발생했습니다.';
        console.error('레시피 로드 오류:', err);
    } finally {
        loading.value = false;
    }
};

// 새 레시피 등록
import { useRouter } from 'vue-router';
const router = useRouter();

const handleCreateRecipe = async () => {
    router.push('/my/recipes/new');
};

// 레시피 수정
const handleEditRecipe = async (recipe: Recipe) => {
    // TODO: 레시피 수정 폼 모달이나 페이지로 이동하는 로직 구현
    console.log('레시피 수정:', recipe);
};

// 레시피 삭제
const handleDeleteRecipe = async (id: number) => {
    if (confirm('정말로 이 레시피를 삭제하시겠습니까?')) {
        loading.value = true;
        error.value = null;
        try {
            await deleteRecipe(id);
            // 삭제 후 목록 새로고침
            await loadRecipes();
        } catch (err) {
            error.value = '레시피 삭제 중 오류가 발생했습니다.';
            console.error('레시피 삭제 오류:', err);
        } finally {
            loading.value = false;
        }
    }
};

// 필터링된 레시피 목록
const filteredRecipes = computed(() => {
    const q = search.value.trim();
    if (!q) return rows.value;
    return rows.value.filter((r) => r.title.includes(q));
});

// 컴포넌트 마운트 시 레시피 목록 로드
onMounted(() => {
    loadRecipes();
});
</script>

<style scoped></style>
