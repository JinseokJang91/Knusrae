<template>
    <div class="card">
        <div class="flex items-center justify-between mb-4">
            <h2 class="text-2xl font-bold">레시피 관리</h2>
            <div class="flex gap-2">
                <input v-model="search" type="text" class="px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-gray-500" placeholder="레시피 검색" />
                <button @click="handleCreateRecipe" class="px-4 py-2 bg-gray-500 text-white rounded-md hover:bg-gray-600 disabled:opacity-50" :disabled="loading">
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
                    <tr class="bg-gray-100 text-left">
                        <th class="px-3 py-2 w-16">#</th>
                        <th class="px-3 py-2">제목</th>
                        <th class="px-3 py-2 w-28">상태</th>
                        <th class="px-3 py-2 w-28">공개</th>
                        <th class="px-3 py-2 w-48 text-right">액션</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-if="filteredRecipes.length === 0">
                        <td colspan="5" class="px-3 py-8 text-center text-gray-500">등록된 레시피가 없습니다.</td>
                    </tr>
                    <tr v-for="(r, i) in filteredRecipes" :key="r.id" class="border-t hover:bg-gray-50">
                        <td class="px-3 py-2">{{ i + 1 }}</td>
                        <td class="px-3 py-2">
                            <button
                                @click="viewRecipeDetail(r.id)"
                                class="text-left text-gray-600 hover:text-gray-800 hover:underline font-medium"
                            >
                                {{ r.title }}
                            </button>
                        </td>
                        <td class="px-3 py-2">
                            <span class="px-2 py-1 rounded text-xs" :class="r.status === 'DRAFT' ? 'bg-yellow-100 text-yellow-700' : 'bg-orange-100 text-orange-700'">
                                {{ r.status === 'DRAFT' ? '초안' : '발행' }}
                            </span>
                        </td>
                        <td class="px-3 py-2">
                            <span class="px-2 py-1 rounded text-xs" :class="r.visibility === 'PUBLIC' ? 'bg-gray-100 text-gray-700' : 'bg-gray-100 text-gray-700'">
                                {{ r.visibility === 'PUBLIC' ? '공개' : '비공개' }}
                            </span>
                        </td>
                        <td class="px-3 py-2 text-right">
                            <button @click="handleEditRecipe(r)" class="px-2 py-1 text-gray-600 hover:bg-gray-100 rounded" :disabled="loading" title="수정">
                                <span class="pi pi-pencil"></span>
                            </button>
                            <button @click="handleDeleteRecipe(r.id)" class="px-2 py-1 text-red-600 hover:bg-red-100 rounded" :disabled="loading" title="삭제">
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
import { httpJson } from '@/utils/http';
import { fetchMemberInfo } from '@/utils/auth';
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useConfirm } from 'primevue/useconfirm';
import { getApiBaseUrl } from '@/utils/constants';
import type { Recipe } from '@/types/recipe';

// API 호출을 위한 기본 URL 및 공용 HTTP 유틸
const API_COOK_BASE_URL = getApiBaseUrl('cook');
const router = useRouter();
const confirm = useConfirm();

// 1. 레시피 목록 조회 (로그인 유저의 레시피)
const fetchRecipes = async (): Promise<Recipe[]> => {
    const currentMember = await fetchMemberInfo();
    if (!currentMember?.id) return [];
    return await httpJson(API_COOK_BASE_URL, `/api/recipe/list/member/${currentMember.id}`, { method: 'GET' });
};

// 2. 레시피 삭제
const deleteRecipe = async (id: number): Promise<void> => {
    await httpJson(API_COOK_BASE_URL, `/api/recipe/${id}`, { method: 'DELETE' });
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

const handleCreateRecipe = async () => {
    router.push('/my/recipes/new');
};

// 레시피 수정
const handleEditRecipe = async (recipe: Recipe) => {
    router.push(`/my/recipes/${recipe.id}/edit`);
};

// 레시피 삭제
const handleDeleteRecipe = async (id: number) => {
    confirm.require({
        message: '정말로 이 레시피를 삭제하시겠습니까?',
        header: '안내',
        icon: 'pi pi-info-circle',
        rejectProps: {
            label: '취소',
            severity: 'secondary',
            outlined: true
        },
        acceptProps: {
            label: '확인'
        },
        accept: async () => {
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
        },
        reject: () => {
            // 취소 시 아무것도 하지 않음
        }
    });
};

// 레시피 상세 페이지로 이동
const viewRecipeDetail = (id: number) => {
    router.push(`/recipe/${id}`);
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
