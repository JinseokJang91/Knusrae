<script setup lang="ts">
import PageStateBlock from '@/components/common/PageStateBlock.vue';
import RecipeGridCard from '@/components/recipe/RecipeGridCard.vue';
import type { RecipeGridItem } from '@/types/recipe';
import { getFavorites, removeFavorite as removeFavoriteApi } from '@/api/recipeApi';
import { fetchMemberInfo } from '@/utils/auth';
import Paginator from 'primevue/paginator';
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import type { FavoriteItem, Recipe, RecipeCookingTip } from '@/types/recipe';

/** cookingTips를 가진 레시피 (찜 목록 API 응답 등) */
type RecipeWithTips = Recipe & { cookingTips?: RecipeCookingTip[] };
import type { PageState } from 'primevue/paginator';

const router = useRouter();

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

        favoriteRecipes.value = await getFavorites(currentMemberId.value);
    } catch (err: unknown) {
        console.error('찜 목록 로드 실패:', err);
        error.value = (err instanceof Error ? err.message : null) || '찜 목록을 불러오는데 실패했습니다.';
        favoriteRecipes.value = [];
    } finally {
        loading.value = false;
    }
};

const onRemoveFavorite = async (recipeId: number): Promise<void> => {
    if (!currentMemberId.value) {
        return;
    }

    try {
        await removeFavoriteApi(currentMemberId.value, recipeId);

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

/** RecipeGridCard에 넘길 레시피 객체 (찜 목록용) */
const getRecipeGridItem = (favorite: FavoriteItem): RecipeGridItem => ({
    id: favorite.recipe.id,
    title: favorite.recipe.title,
    thumbnail: favorite.recipe.thumbnail,
    cookingTime: getCookingTime(favorite.recipe) ?? undefined,
    servings: getServings(favorite.recipe) ?? undefined,
    hits: favorite.recipe.hits,
    isFavorite: true,
    memberNickname: favorite.recipe.memberNickname,
    memberName: favorite.recipe.memberName,
    memberProfileImage: favorite.recipe.memberProfileImage
});

const formatDate = (dateString: string): string => {
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR');
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

<template>
    <div class="page-container page-container--card page-container--wide">
        <div class="favorites-content">
            <div class="mb-6 p-4 bg-orange-50 border-l-4 border-orange-500 rounded-r">
                <p class="text-gray-700 italic">찜 버튼( <i class="pi pi-heart-fill" /> )을 클릭해 찜 목록에서 삭제할 수 있어요.</p>
            </div>

            <!-- body : 레시피 목록 섹션 -->
            <div class="recipe-section">
                <div class="flex justify-between items-center mb-3">
                    <h2 class="text-2xl font-semibold text-gray-900 m-0">내가 찜한 레시피 ({{ totalFavorites }})</h2>
                </div>

                <!-- 로딩 / 에러 / 빈 상태 -->
                <PageStateBlock v-if="loading" state="loading" loading-message="찜 목록을 불러오는 중..." />
                <PageStateBlock v-else-if="error" state="error" error-title="찜 목록을 불러올 수 없습니다" :error-message="error" retry-label="다시 시도" @retry="loadFavorites" />
                <PageStateBlock
                    v-else-if="displayFavorites.length === 0"
                    state="empty"
                    empty-icon="pi pi-heart"
                    empty-title="찜한 레시피가 없습니다"
                    empty-message="마음에 드는 레시피를 찜해보세요!"
                    empty-button-label="레시피 둘러보기"
                    @empty-action="browseRecipes"
                />

                <!-- 레시피 목록이 있는 경우 -->
                <template v-else>
                    <div class="recipe-grid">
                        <RecipeGridCard
                            v-for="favorite in displayFavorites"
                            :key="favorite.id"
                            :recipe="getRecipeGridItem(favorite)"
                            :category-label="getCategoryName(favorite.recipe)"
                            :date-text="formatDate(favorite.createdAt)"
                            :show-bookmark="false"
                            :show-comment-count="false"
                            :favorites-mode="true"
                            :show-author="false"
                            @click="viewRecipe"
                            @favorite="onRemoveFavorite"
                        />
                    </div>
                    <div class="flex justify-center mt-4">
                        <Paginator v-model:first="first" :rows="rows" :totalRecords="totalFavorites" @page="onPageChange" template="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink" />
                    </div>
                </template>
            </div>
        </div>
    </div>
</template>

<style scoped>
/* 레시피 목록 카드 스타일은 layout _recipe-card-list.scss 공통 사용 */
</style>
