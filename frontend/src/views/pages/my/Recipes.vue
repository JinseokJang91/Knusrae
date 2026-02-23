<script setup lang="ts">
import { getRecipeListByMember, deleteRecipe } from '@/api/recipeApi';
import { fetchMemberInfo } from '@/utils/auth';
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useConfirm } from 'primevue/useconfirm';
import Button from 'primevue/button';
import PageStateBlock from '@/components/common/PageStateBlock.vue';
import type { Recipe } from '@/types/recipe';

const router = useRouter();
const confirm = useConfirm();

const fetchRecipes = async (): Promise<Recipe[]> => {
    const currentMember = await fetchMemberInfo();
    if (!currentMember?.id) return [];
    return await getRecipeListByMember(currentMember.id);
};

const deleteRecipeById = async (id: number): Promise<void> => {
    await deleteRecipe(id);
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

const goToMypage = () => {
    router.push('/my');
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
                await deleteRecipeById(id);
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

<template>
    <div class="page-container page-container--card">
        <!-- 브레드크럼 -->
        <nav class="text-sm text-gray-500 mb-2" aria-label="breadcrumb">
            <router-link to="/my" class="text-gray-500 hover:text-gray-700 hover:underline">마이페이지</router-link>
            <span class="mx-1">/</span>
            <span class="text-gray-700 font-medium" aria-current="page">레시피 관리</span>
        </nav>

        <div class="flex items-center justify-between mb-4 flex-wrap gap-2">
            <h2 class="text-2xl font-bold m-0">레시피 관리</h2>
            <div class="flex gap-2 flex-wrap">
                <Button label="마이페이지" icon="pi pi-user" severity="secondary" outlined @click="goToMypage" />
                <input v-model="search" type="text" class="px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-gray-500" placeholder="레시피 검색" :disabled="loading" />
                <Button label="레시피 등록하기" icon="pi pi-plus" :disabled="loading" @click="handleCreateRecipe" />
            </div>
        </div>

        <!-- 로딩 -->
        <PageStateBlock v-if="loading" state="loading" loading-message="레시피 목록을 불러오는 중..." />

        <!-- 에러 -->
        <PageStateBlock v-else-if="error" state="error" error-title="레시피 목록을 불러올 수 없습니다" :error-message="error" retry-label="다시 시도" @retry="loadRecipes" />

        <!-- 빈 상태 -->
        <PageStateBlock
            v-else-if="filteredRecipes.length === 0"
            state="empty"
            empty-icon="pi pi-book"
            empty-title="등록된 레시피가 없습니다"
            empty-message="첫 레시피를 등록해 보세요."
            empty-button-label="레시피 등록하기"
            @empty-action="handleCreateRecipe"
        />

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
                    <tr v-for="(r, i) in filteredRecipes" :key="r.id" class="border-t hover:bg-gray-50">
                        <td class="px-3 py-2">{{ i + 1 }}</td>
                        <td class="px-3 py-2">
                            <button @click="viewRecipeDetail(r.id)" class="text-left text-gray-600 hover:text-gray-800 hover:underline font-medium">
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

<style scoped></style>
