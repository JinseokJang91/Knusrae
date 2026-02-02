<template>
    <div class="favorites-content">
        <div class="mb-6 p-4 bg-orange-50 border-l-4 border-orange-500 rounded-r">
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
                <!-- 그리드 뷰 (카드 형태) -->
                <div class="recipe-grid">
                    <div v-for="favorite in displayFavorites" :key="favorite.id" class="recipe-card-wrapper" @click="viewRecipe(favorite.recipeId)">
                        <Card class="recipe-card h-full">
                            <template #header>
                                <div class="recipe-image-container">
                                    <img :src="favorite.recipe.thumbnail || 'https://images.unsplash.com/photo-1565299624946-b28f40a0ca4b?w=400'" :alt="favorite.recipe.title" class="recipe-image" />
                                    <div class="recipe-overlay">
                                        <div class="recipe-actions">
                                            <Button icon="pi pi-heart-fill" class="p-button-danger" size="large" rounded @click.stop="removeFavorite(favorite.recipeId)" />
                                        </div>
                                        <Tag v-if="getCategoryName(favorite.recipe)" :value="getCategoryName(favorite.recipe)" severity="info" class="recipe-category-tag" />
                                    </div>
                                    <div v-if="formatCount(favorite.recipe.hits)" class="recipe-hits-overlay">
                                        조회수 {{ formatCount(favorite.recipe.hits) }}
                                    </div>
                                </div>
                            </template>
                            <template #content>
                                <div class="recipe-content">
                                    <h3 class="recipe-title">{{ favorite.recipe.title }}</h3>
                                    <div class="recipe-meta">
                                        <div class="recipe-info">
                                            <div v-if="getCookingTime(favorite.recipe)" class="info-item">
                                                <i class="pi pi-clock"></i>
                                                <span>{{ getCookingTime(favorite.recipe) }}</span>
                                            </div>
                                            <div v-if="getServings(favorite.recipe)" class="info-item">
                                                <i class="pi pi-users"></i>
                                                <span>{{ getServings(favorite.recipe) }}</span>
                                            </div>
                                        </div>
                                        <div class="text-sm text-gray-500 mt-1">
                                            <i class="pi pi-calendar"></i>
                                            <span>{{ formatDate(favorite.createdAt) }}</span>
                                        </div>
                                    </div>
                                </div>
                            </template>
                        </Card>
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

<script setup lang="ts">
import { httpJson } from '@/utils/http';
import { fetchMemberInfo } from '@/utils/auth';
import Button from 'primevue/button';
import Card from 'primevue/card';
import Paginator from 'primevue/paginator';
import ProgressSpinner from 'primevue/progressspinner';
import Tag from 'primevue/tag';
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { getApiBaseUrl } from '@/utils/constants';
import type { FavoriteItem, Recipe, RecipeCookingTip } from '@/types/recipe';

/** cookingTips를 가진 레시피 (찜 목록 API 응답 등) */
type RecipeWithTips = Recipe & { cookingTips?: RecipeCookingTip[] };
import type { PageState } from 'primevue/paginator';

const router = useRouter();

// API 기본 URL
const API_BASE_URL = getApiBaseUrl('cook');

// 반응형 데이터
const favoriteRecipes = ref<FavoriteItem[]>([]);
const currentMemberId = ref<number | null>(null);
const first = ref(0);
const rows = ref(12);
const loading = ref(false);
const error = ref<string | null>(null);

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

        favoriteRecipes.value = (response as FavoriteItem[]) || [];
    } catch (err: unknown) {
        console.error('찜 목록 로드 실패:', err);
        error.value = (err instanceof Error ? err.message : null) || '찜 목록을 불러오는데 실패했습니다.';
        favoriteRecipes.value = [];
    } finally {
        loading.value = false;
    }
};

const removeFavorite = async (recipeId: number): Promise<void> => {
    if (!currentMemberId.value) {
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
    } catch (err: unknown) {
        console.error('찜 삭제 실패:', err);
    }
};

const viewRecipe = (recipeId: number): void => {
    router.push(`/recipe/${recipeId}`);
};

const browseRecipes = (): void => {
    router.push('/recipe/category');
};

const formatDate = (dateString: string): string => {
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR');
};

// 조회수 포맷팅 (만/억 단위 처리)
const formatCount = (count: number | undefined | null): string | null => {
    if (!count || count === 0) return null;
    if (count >= 100000000) {
        const eok = count / 100000000;
        const rounded = Math.round(eok * 10) / 10;
        return rounded % 1 === 0 ? `${Math.round(rounded)}억` : `${rounded}억`;
    }
    if (count >= 10000) {
        const man = count / 10000;
        const rounded = Math.round(man * 10) / 10;
        return rounded % 1 === 0 ? `${Math.round(rounded)}만` : `${rounded}만`;
    }
    return count.toLocaleString();
};

const onPageChange = (event: PageState): void => {
    first.value = event.first;
    rows.value = event.rows;
};

// cookingTips에서 요리 시간 추출
const getCookingTime = (recipe: RecipeWithTips | undefined | null): string | null => {
    if (!recipe || !recipe.cookingTips || !Array.isArray(recipe.cookingTips)) {
        return null;
    }
    const cookingTimeTip = recipe.cookingTips.find((tip: RecipeCookingTip) => tip.codeId === 'COOKING_TIME');
    return cookingTimeTip?.detailName || null;
};

// cookingTips에서 인분 수 추출
const getServings = (recipe: RecipeWithTips | undefined | null): string | null => {
    if (!recipe || !recipe.cookingTips || !Array.isArray(recipe.cookingTips)) {
        return null;
    }
    const servingTip = recipe.cookingTips.find((tip: RecipeCookingTip) => tip.codeId === 'SERVINGS');
    return servingTip?.detailName || null;
};

// 카테고리 이름 추출
const getCategoryName = (recipe: Recipe | undefined | null): string | null => {
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
        }
    };
    initializeFavorites();
});
</script>

<style scoped>
/* 레시피 목록 카드 스타일은 layout _recipe-card-list.scss 공통 사용 */
</style>
