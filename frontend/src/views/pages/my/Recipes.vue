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

        <!-- 레시피 상세 모달 -->
        <div v-if="showDetailModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50" @click="closeDetailModal">
            <div class="bg-white rounded-lg max-w-4xl max-h-[90vh] overflow-y-auto m-4" @click.stop>
                <div class="p-6">
                    <div class="flex items-center justify-between mb-4">
                        <h3 class="text-xl font-bold">레시피 상세</h3>
                        <button @click="closeDetailModal" class="px-2 py-1 text-gray-600 hover:bg-gray-100 rounded">
                            <span class="pi pi-times"></span>
                        </button>
                    </div>

                    <div v-if="detailLoading" class="flex justify-center items-center py-8">
                        <div class="pi pi-spinner pi-spin mr-2"></div>
                        <span>레시피 로딩 중...</span>
                    </div>

                    <div v-else-if="recipeDetail">
                        <h4 class="text-lg font-semibold mb-2">{{ recipeDetail.title }}</h4>
                        <p class="text-gray-600 mb-4">{{ recipeDetail.introduction }}</p>

                        <!-- 이미지 갤러리 -->
                        <div v-if="recipeDetail.images && recipeDetail.images.length > 0" class="mb-6">
                            <h5 class="font-medium mb-2">이미지</h5>
                            <div class="grid grid-cols-2 md:grid-cols-3 gap-4">
                                <div v-for="(image, index) in recipeDetail.images" :key="index" class="relative border rounded-lg overflow-hidden" :class="{ 'ring-2 ring-gray-500': image.isMainImage }">
                                    <img :src="image.url" :alt="`레시피 이미지 ${index + 1}`" class="w-full h-32 object-cover" />
                                    <div v-if="image.isMainImage" class="absolute top-2 left-2 bg-gray-500 text-white text-xs px-1 rounded">메인</div>
                                </div>
                            </div>
                        </div>

                        <!-- 레시피 스텝 -->
                        <div v-if="recipeDetail.steps && recipeDetail.steps.length > 0">
                            <h5 class="font-medium mb-2">조리 단계</h5>
                            <div class="space-y-2">
                                <div v-for="(step, index) in recipeDetail.steps" :key="index" class="flex gap-3 p-3 bg-gray-50 rounded">
                                    <span class="font-bold text-gray-600">{{ index + 1 }}.</span>
                                    <span>{{ step.text }}</span>
                                </div>
                            </div>
                        </div>

                        <div class="mt-4 pt-4 border-t text-sm text-gray-500">
                            <div>카테고리: {{ formatCategories(recipeDetail.categories) }}</div>
                            <div>상태: {{ recipeDetail.status }}</div>
                            <div>공개 설정: {{ recipeDetail.visibility }}</div>
                            <div>조회수: {{ recipeDetail.hits || 0 }}</div>
                            <div v-if="recipeDetail.createdAt">등록일: {{ new Date(recipeDetail.createdAt).toLocaleDateString() }}</div>
                        </div>
                    </div>
                </div>
            </div>
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
import type { Recipe, RecipeDetail } from '@/types/recipe';

// API 호출을 위한 기본 URL 및 공용 HTTP 유틸
const API_COOK_BASE_URL = getApiBaseUrl('cook');
const router = useRouter();
const confirm = useConfirm();

// 1. 레시피 목록 조회 (로그인 유저의 레시피)
const fetchRecipes = async (): Promise<Recipe[]> => {
    const currentMember = await fetchMemberInfo();
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
const showDetailModal = ref(false);
const detailLoading = ref(false);
const recipeDetail = ref<RecipeDetail | null>(null);

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

// 레시피 상세 모달 닫기
const closeDetailModal = () => {
    showDetailModal.value = false;
    recipeDetail.value = null;
};

// 레시피 상세 페이지로 이동
const viewRecipeDetail = (id: number) => {
    router.push(`/recipe/${id}`);
};

const formatCategories = (categories?: Array<{ codeName?: string; detailName?: string }>) => {
    if (!categories || categories.length === 0) {
        return '-';
    }
    return categories
        .map((category) => category.detailName || category.codeName)
        .filter((name): name is string => Boolean(name && name.trim()))
        .join(', ');
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
