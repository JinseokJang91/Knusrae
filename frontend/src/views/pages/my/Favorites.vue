<template>
    <div class="card">
        <!-- header : 페이지 제목 -->
        <div class="flex justify-between items-center mb-4">
            <h1 class="text-3xl font-bold text-gray-900">찜 목록</h1>
        </div>

        <div class="mb-6 p-4 bg-green-50 border-l-4 border-green-500 rounded-r">
            <p class="text-gray-700 italic">
                찜 버튼( <i class="pi pi-heart-fill"/> )을 클릭해 찜 목록에서 삭제할 수 있어요.
            </p>
        </div>

        <!-- body : 레시피 목록 섹션 -->
        <div class="recipe-section">
            <div class="flex justify-between items-center mb-3">
                <h2 class="text-2xl font-semibold text-gray-900 m-0">
                    내가 찜한 레시피 ({{ totalFavorites }})
                </h2>
            </div>

            <!-- 로딩 상태 -->
            <div v-if="loading" class="text-center py-8">
                <ProgressSpinner />
                <p class="text-gray-600 mt-3">찜 목록을 불러오는 중...</p>
            </div>

            <!-- 에러 상태 -->
            <div v-else-if="error" class="text-center py-8">
                <i class="pi pi-exclamation-triangle text-6xl text-red-500 mb-4"></i>
                <h3 class="text-2xl font-semibold text-gray-600 mb-2">찜 목록을 불러올 수 없습니다</h3>
                <p class="text-gray-600 mb-4">{{ error }}</p>
                <Button label="다시 시도" @click="loadFavorites" />
            </div>

            <!-- 레시피 목록이 있는 경우 -->
            <div v-else-if="displayFavorites.length > 0">
                <!-- 리스트 뷰 -->
                <div class="recipe-list">
                    <div v-for="favorite in displayFavorites" :key="favorite.id" class="recipe-list-item">
                        <div class="flex items-center gap-3 p-3 rounded hover:bg-gray-50 transition-colors duration-150">
                            <img :src="favorite.recipe.thumbnail || 'https://images.unsplash.com/photo-1565299624946-b28f40a0ca4b?w=400'" :alt="favorite.recipe.title" class="recipe-thumbnail" @click="viewRecipe(favorite.recipeId)" />
                            <div class="flex-1" @click="viewRecipe(favorite.recipeId)">
                                <h4 class="text-lg font-semibold text-gray-900 m-0 mb-1">{{ favorite.recipe.title }}</h4>
                                <p class="text-sm text-gray-600 mb-2">{{ favorite.recipe.description }}</p>
                                <div class="flex items-center gap-3 text-sm text-gray-500">
                                    <div v-if="favorite.recipe.hits !== null && favorite.recipe.hits !== undefined" class="flex items-center gap-1">
                                        <i class="pi pi-eye"></i>
                                        <span>{{ favorite.recipe.hits.toLocaleString() }}</span>
                                    </div>
                                    <div v-if="getCookingTime(favorite.recipe)" class="flex items-center gap-1">
                                        <i class="pi pi-clock"></i>
                                        <span>{{ getCookingTime(favorite.recipe) }}</span>
                                    </div>
                                    <div v-if="getServings(favorite.recipe)" class="flex items-center gap-1">
                                        <i class="pi pi-users"></i>
                                        <span>{{ getServings(favorite.recipe) }}</span>
                                    </div>
                                    <div v-if="getCategoryName(favorite.recipe)" class="flex items-center gap-1">
                                        <Tag :value="getCategoryName(favorite.recipe)" severity="info" />
                                    </div>
                                    <div class="flex items-center gap-1">
                                        <i class="pi pi-calendar"></i>
                                        <span>{{ formatDate(favorite.createdAt) }}</span>
                                    </div>
                                </div>
                            </div>
                            <div class="flex flex-col gap-2">
                                <Button icon="pi pi-heart-fill" class="p-button-danger" size="small" rounded @click="removeFavorite(favorite.recipeId)" />
                                <Button label="상세보기" size="small" @click="viewRecipe(favorite.recipeId)" />
                            </div>
                        </div>
                    </div>
                </div>

                <!-- footer : 페이지네이션 -->
                <div class="flex justify-center mt-4">
                    <Paginator v-model:first="first" :rows="rows" :totalRecords="totalFavorites" @page="onPageChange" template="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink" />
                </div>
            </div>

            <!-- 빈 상태 -->
            <div v-else class="text-center py-8">
                <i class="pi pi-heart text-6xl text-300 mb-4"></i>
                <h3 class="text-2xl font-semibold text-gray-600 mb-2">찜한 레시피가 없습니다</h3>
                <p class="text-gray-600 mb-4">마음에 드는 레시피를 찜해보세요!</p>
                <Button label="레시피 둘러보기" @click="browseRecipes" />
            </div>
        </div>
    </div>
</template>

<script setup>
import { httpJson } from '@/utils/http';
import { fetchMemberInfo } from '@/utils/auth';
import Button from 'primevue/button';
import Paginator from 'primevue/paginator';
import ProgressSpinner from 'primevue/progressspinner';
import Tag from 'primevue/tag';
import { useToast } from 'primevue/usetoast';
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { getApiBaseUrl } from '@/utils/constants';

const router = useRouter();
const toast = useToast();

// API 기본 URL
const API_BASE_URL = getApiBaseUrl('cook');

// 반응형 데이터
const favoriteRecipes = ref([]);
const currentMemberId = ref(null);
const first = ref(0);
const rows = ref(12);
const loading = ref(false);
const error = ref(null);

// 계산된 속성
const totalFavorites = computed(() => favoriteRecipes.value.length);

const displayFavorites = computed(() => {
    return favoriteRecipes.value.slice(first.value, first.value + rows.value);
});

// 메서드
const loadFavorites = async () => {
    if (!currentMemberId.value) {
        error.value = '로그인이 필요합니다.';
        return;
    }

    try {
        loading.value = true;
        error.value = null;

        const response = await httpJson(
            API_BASE_URL,
            `/api/recipe/favorites/${currentMemberId.value}`,
            { method: 'GET' }
        );

        favoriteRecipes.value = response || [];

        toast.add({
            severity: 'success',
            summary: '찜 목록 로드 완료',
            detail: `${favoriteRecipes.value.length}개의 찜한 레시피를 불러왔습니다.`,
            life: 3000
        });
    } catch (err) {
        console.error('찜 목록 로드 실패:', err);
        error.value = err.message || '찜 목록을 불러오는데 실패했습니다.';
        favoriteRecipes.value = [];

        toast.add({
            severity: 'error',
            summary: '로드 실패',
            detail: '찜 목록을 불러올 수 없습니다.',
            life: 3000
        });
    } finally {
        loading.value = false;
    }
};

const removeFavorite = async (recipeId) => {
    if (!currentMemberId.value) {
        toast.add({ severity: 'error', summary: '오류', detail: '로그인이 필요합니다.', life: 3000 });
        return;
    }

    try {
        await httpJson(
            API_BASE_URL,
            `/api/recipe/favorites?memberId=${currentMemberId.value}&recipeId=${recipeId}`,
            { method: 'DELETE' }
        );

        // 로컬 상태에서 제거
        favoriteRecipes.value = favoriteRecipes.value.filter((fav) => fav.recipeId !== recipeId);

        toast.add({ 
            severity: 'success', 
            summary: '찜 해제', 
            detail: '찜 목록에서 제거되었습니다.', 
            life: 3000 
        });
    } catch (err) {
        console.error('찜 삭제 실패:', err);
        toast.add({ 
            severity: 'error', 
            summary: '삭제 실패', 
            detail: '찜 목록에서 제거할 수 없습니다.', 
            life: 3000 
        });
    }
};

const viewRecipe = (recipeId) => {
    router.push(`/recipe/${recipeId}`);
};

const browseRecipes = () => {
    router.push('/recipe/category');
};

const formatDate = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR');
};

const onPageChange = (event) => {
    first.value = event.first;
    rows.value = event.rows;
};

// cookingTips에서 요리 시간 추출
const getCookingTime = (recipe) => {
    if (!recipe || !recipe.cookingTips || !Array.isArray(recipe.cookingTips)) {
        return null;
    }
    const cookingTimeTip = recipe.cookingTips.find((tip) => tip.codeId === 'COOKING_TIME');
    return cookingTimeTip?.detailName || null;
};

// cookingTips에서 인분 수 추출
const getServings = (recipe) => {
    if (!recipe || !recipe.cookingTips || !Array.isArray(recipe.cookingTips)) {
        return null;
    }
    const servingTip = recipe.cookingTips.find((tip) => tip.codeId === 'SERVINGS');
    return servingTip?.detailName || null;
};

// 카테고리 이름 추출
const getCategoryName = (recipe) => {
    if (!recipe || !recipe.categories || !Array.isArray(recipe.categories) || recipe.categories.length === 0) {
        return null;
    }
    const keywordCategory = recipe.categories.find((cat) => cat.codeId === 'COOKING_KEYWORD');
    const target = keywordCategory || recipe.categories[0];
    return target?.detailName || target?.codeName || null;
};

// 생명주기
onMounted(() => {
    const initializeFavorites = async () => {
        // 사용자 정보 가져오기
        const memberInfo = await fetchMemberInfo();
        if (memberInfo && memberInfo.id) {
            currentMemberId.value = memberInfo.id;
            await loadFavorites();
        } else {
            error.value = '로그인이 필요합니다.';
            toast.add({
                severity: 'warn',
                summary: '로그인 필요',
                detail: '찜 목록을 보려면 로그인해주세요.',
                life: 3000
            });
        }
    };
    initializeFavorites();
});
</script>

<style scoped>
.recipe-section {
    margin-top: 2rem;
    padding-top: 2rem;
    border-top: 1px solid var(--surface-border);
}

.recipe-list-item {
    border-bottom: 1px solid var(--surface-border);
    cursor: pointer;
}

.recipe-list-item:last-child {
    border-bottom: none;
}

.recipe-thumbnail {
    width: 120px;
    height: 120px;
    min-width: 120px;
    min-height: 120px;
    object-fit: cover;
    object-position: center;
    border-radius: 8px;
    display: block;
    flex-shrink: 0;
    cursor: pointer;
}
</style>
