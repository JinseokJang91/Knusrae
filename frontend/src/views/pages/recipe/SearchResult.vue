<template>
    <div class="card">
        <!-- header : 페이지 제목 -->
        <div class="flex justify-between items-center mb-4">
            <h1 class="text-3xl font-bold text-gray-900">
                검색 결과
                <span v-if="searchKeyword" class="text-xl text-primary">"{{ searchKeyword }}"</span>
            </h1>
        </div>

        <!-- body : 레시피 목록 섹션 -->
        <div class="recipe-section">
            <div class="flex justify-between items-center mb-3">
                <div class="flex items-center gap-3">
                    <h2 class="text-2xl font-bold text-gray-900 m-0">검색 결과</h2>
                    <div class="recipe-count-bubble">
                        <span class="text-primary font-bold">{{ totalDisplayRecipes.toLocaleString() }}</span>개의 레시피가 준비되어 있어요!
                    </div>
                </div>
                <SelectButton
                    v-model="sortBy"
                    :options="sortOptions"
                    optionLabel="label"
                    optionValue="value"
                    class="recipe-sort-buttons"
                    @change="onSortChange"
                />
            </div>

            <!-- 로딩 / 에러 / 빈 상태 -->
            <PageStateBlock
                v-if="loading"
                state="loading"
                loading-message="검색 중..."
            />
            <PageStateBlock
                v-else-if="error"
                state="error"
                error-title="검색 중 오류가 발생했습니다"
                :error-message="error"
                retry-label="다시 시도"
                @retry="performSearch"
            />
            <PageStateBlock
                v-else-if="displayRecipes.length === 0"
                state="empty"
                empty-icon="pi pi-search"
                empty-title="검색 결과가 없습니다"
                :empty-message="emptyMessage"
                empty-button-label="다시 검색"
                @empty-action="goToHome"
            />

            <!-- 검색 결과가 있는 경우 -->
            <template v-else>
                <div class="recipe-grid">
                    <RecipeGridCard
                        v-for="recipe in displayRecipes"
                        :key="recipe.id"
                        :recipe="recipe"
                        :category-label="getCategoryName(recipe.category)"
                        :highlight-keyword="searchKeyword || null"
                        :is-bookmarked="bookmarkedRecipeIds.has(recipe.id)"
                        show-bookmark
                        show-comment-count
                        @click="viewRecipe"
                        @favorite="toggleFavorite"
                        @bookmark="bookmarkRecipe"
                        @scroll-to-comments="scrollToComments"
                    />
                </div>
                <div class="flex justify-center mt-4">
                    <Paginator v-model:first="first" :rows="rows" :totalRecords="totalDisplayRecipes" @page="onPageChange" template="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink" />
                </div>
            </template>
        </div>

        <!-- 북마크 Dialog -->
        <BookmarkDialog
            v-model:visible="bookmarkDialogVisible"
            :recipe-id="bookmarkRecipeId"
            @bookmarked="onBookmarked"
        />
    </div>
</template>

<script setup lang="ts">
import { getCommonCodesByGroup } from '@/api/commonCodeApi';
import { getFavorites, toggleFavorite as toggleFavoriteApi } from '@/api/recipeApi';
import { getFolders, getBookmarksByFolder } from '@/api/bookmarkApi';
import { useAuthStore } from '@/stores/authStore';
import PageStateBlock from '@/components/common/PageStateBlock.vue';
import RecipeGridCard from '@/components/recipe/RecipeGridCard.vue';
import BookmarkDialog from '@/components/bookmark/BookmarkDialog.vue';
import { searchRecipes } from '@/utils/search';
import { useAppToast } from '@/utils/toast';
import SelectButton from 'primevue/selectbutton';
import Paginator from 'primevue/paginator';
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import type { PageState } from 'primevue/paginator';
import type { Recipe, RecipeCategory } from '@/types/recipe';

/** 검색 결과 목록용 레시피 (카테고리/찜 등 UI 필드 포함) */
type SearchResultRecipe = Recipe & {
    categoryKeys: string[];
    categoryIds: string[];
    category: string | null;
    cookingTime: string | null;
    servings: string | null;
    isFavorite: boolean;
    commentCount: number;
};

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();
const { showWarn } = useAppToast();

// 북마크 Dialog
const bookmarkDialogVisible = ref(false);
const bookmarkRecipeId = ref<number | null>(null);
// 북마크된 레시피 ID 집합 (카드 선택 상태 표시용)
const bookmarkedRecipeIds = ref<Set<number>>(new Set());

// 반응형 데이터
const recipes = ref<SearchResultRecipe[]>([]);
const first = ref(0);
const rows = ref(12);
const loading = ref(false);
const error = ref<string | null>(null);
const searchKeyword = ref<string>('');

// 현재 로그인한 사용자 정보
const currentMemberId = computed(() => authStore.memberInfo?.id || null);

// 카테고리 목록 (카테고리 이름 표시용)
const categories = ref<Array<{ value: string; name: string; mainCategoryId?: string }>>([]);

// 정렬 옵션 (최신순, 조회순, 댓글순)
const SORT_LATEST = 'latest';
const SORT_HITS = 'hits';
const SORT_COMMENTS = 'comments';
const sortOptions = [
    { label: '최신순', value: SORT_LATEST },
    { label: '조회순', value: SORT_HITS },
    { label: '댓글순', value: SORT_COMMENTS }
];
const sortBy = ref<string>(SORT_LATEST);

// 정렬된 레시피 목록
const sortedRecipes = computed(() => {
    const list = [...recipes.value];
    if (sortBy.value === SORT_LATEST) {
        list.sort((a, b) => {
            const dateA = a.createdAt ? new Date(a.createdAt).getTime() : 0;
            const dateB = b.createdAt ? new Date(b.createdAt).getTime() : 0;
            return dateB - dateA;
        });
    } else if (sortBy.value === SORT_HITS) {
        list.sort((a, b) => (b.hits ?? 0) - (a.hits ?? 0));
    } else if (sortBy.value === SORT_COMMENTS) {
        list.sort((a, b) => (b.commentCount ?? 0) - (a.commentCount ?? 0));
    }
    return list;
});

// 검색 결과 표시 (정렬된 목록에서 페이지 슬라이스)
const displayRecipes = computed(() => {
    return sortedRecipes.value.slice(first.value, first.value + rows.value);
});

// 전체 검색 결과 개수
const totalDisplayRecipes = computed(() => {
    return sortedRecipes.value.length;
});

// 빈 상태 메시지
const emptyMessage = computed(() => {
    if (searchKeyword.value) {
        return `"${searchKeyword.value}"에 대한 검색 결과가 없습니다.`;
    }
    return '검색어를 입력해주세요.';
});

// 카테고리 이름 가져오기
const getCategoryName = (categoryValue: string | null | undefined) => {
    if (!categoryValue) return null;
    const category = categories.value.find((cat) => cat.value === categoryValue && cat.mainCategoryId === 'COOKING_KEYWORD');
    return category ? category.name : null;
};

// 카테고리 목록 로드 (카테고리 이름 표시용)
const loadCategories = async () => {
    try {
        const codes = await getCommonCodesByGroup('CATEGORY');
        categories.value = [];
        codes.forEach((code) => {
            if (Array.isArray(code.details)) {
                code.details.forEach((detail) => {
                    categories.value.push({
                        value: detail.detailCodeId,
                        name: detail.codeName,
                        mainCategoryId: code.codeId
                    });
                });
            }
        });
    } catch (err) {
        // 카테고리 목록 조회 실패 시 무시
    }
};

// 레시피의 카테고리 키 추출
const extractCategoryKeys = (recipe: Recipe) => {
    if (!recipe || !recipe.categories || recipe.categories.length === 0) {
        return [];
    }
    return recipe.categories
        .filter((category) => category.codeId && category.detailCodeId)
        .map((category) => `${category.codeId}-${category.detailCodeId}`);
};

// 레시피의 카테고리 ID 추출
const extractCategoryIds = (recipe: Recipe) => {
    if (!recipe || !recipe.categories || recipe.categories.length === 0) {
        return [];
    }
    return recipe.categories.map((category) => category.detailCodeId || category.codeId).filter(Boolean);
};

// cookingTips에서 요리 시간 추출
const extractCookingTime = (cookingTips: Array<{ codeId: string; detailName?: string }> | undefined) => {
    if (!cookingTips || !Array.isArray(cookingTips)) {
        return null;
    }
    const cookingTimeTip = cookingTips.find((tip) => tip.codeId === 'COOKING_TIME');
    return cookingTimeTip?.detailName || null;
};

// cookingTips에서 인분 수 추출
const extractServings = (cookingTips: Array<{ codeId: string; detailName?: string }> | undefined) => {
    if (!cookingTips || !Array.isArray(cookingTips)) {
        return null;
    }
    const servingTip = cookingTips.find((tip) => tip.codeId === 'SERVINGS');
    return servingTip?.detailName || null;
};

// 검색 수행
const performSearch = async () => {
    const keyword = route.query.keyword as string;
    
    if (!keyword || !keyword.trim()) {
        searchKeyword.value = '';
        recipes.value = [];
        return;
    }

    searchKeyword.value = keyword.trim();

    try {
        loading.value = true;
        error.value = null;

        const searchResults = await searchRecipes(searchKeyword.value);
        
        let favoriteRecipeIds: number[] = [];
        if (currentMemberId.value) {
            try {
                const favoritesList = await getFavorites(currentMemberId.value);
                favoriteRecipeIds = favoritesList.map((fav) => fav.recipeId);
            } catch (err) {
                console.error('찜 목록을 가져올 수 없습니다:', err);
            }
        }
        
        recipes.value = searchResults.map((recipe: Recipe) => {
            const cookingTime = extractCookingTime(recipe.cookingTips);
            const servings = extractServings(recipe.cookingTips);

            const isFavorite = favoriteRecipeIds.includes(recipe.id);
            
            const categoryKeys = extractCategoryKeys(recipe);
            const categoryIds = extractCategoryIds(recipe);
            const keywordCategory = recipe.categories?.find((cat: RecipeCategory) => cat.codeId === 'COOKING_KEYWORD');
            const primaryCategoryId = keywordCategory?.detailCodeId ?? categoryIds[0] ?? null;

            return {
                ...recipe,
                categoryKeys,
                categoryIds,
                category: primaryCategoryId,
                cookingTime,
                servings,
                isFavorite,
                commentCount: recipe.commentCount ?? 0
            } as SearchResultRecipe;
        });

        first.value = 0; // 검색 시 첫 페이지로 이동

        // 북마크된 레시피 ID 목록 로드 (카드 선택 상태 표시용)
        await loadBookmarkedRecipeIds();
    } catch (err: unknown) {
        console.error('검색 실패:', err);
        error.value = err instanceof Error ? err.message : '검색 중 오류가 발생했습니다.';
        recipes.value = [];
    } finally {
        loading.value = false;
    }
};

// 레시피 상세 페이지 댓글 영역으로 이동
const scrollToComments = (recipeId: number) => {
    router.push(`/recipe/${recipeId}#comments`);
};

// 찜 목록 추가/제거
const toggleFavorite = async (recipeId: number) => {
    if (!currentMemberId.value) {
        return;
    }

    try {
        const recipe = recipes.value.find((r) => r.id === recipeId);
        if (!recipe) return;

        const response = await toggleFavoriteApi(currentMemberId.value, recipeId);
        recipe.isFavorite = response.isFavorite;
    } catch (err) {
        console.error('찜 토글 실패:', err);
    }
};

// 레시피 상세 조회
const viewRecipe = (recipeId: number) => {
    router.push(`/recipe/${recipeId}`);
};

// 북마크 Dialog 열기
const bookmarkRecipe = (recipeId: number) => {
    if (!currentMemberId.value) {
        showWarn('로그인이 필요한 기능입니다.');
        router.push({ path: '/auth/login', query: { redirect: route.fullPath } });
        return;
    }
    bookmarkRecipeId.value = recipeId;
    bookmarkDialogVisible.value = true;
};

const onBookmarked = async () => {
    // 토스트는 BookmarkDialog에서 이미 표시됨. 북마크 목록 갱신
    await loadBookmarkedRecipeIds();
};

/** 로그인 사용자의 북마크된 레시피 ID 목록 로드 (카드 선택 상태 표시용) */
const loadBookmarkedRecipeIds = async () => {
    if (!currentMemberId.value) {
        bookmarkedRecipeIds.value = new Set();
        return;
    }
    try {
        const folders = await getFolders();
        const ids = new Set<number>();
        for (const folder of folders) {
            const bookmarks = await getBookmarksByFolder(folder.id);
            bookmarks.forEach((b) => ids.add(b.recipeId));
        }
        bookmarkedRecipeIds.value = ids;
    } catch {
        bookmarkedRecipeIds.value = new Set();
    }
};

// 정렬 변경 시 첫 페이지로
const onSortChange = () => {
    first.value = 0;
};

// 페이징
const onPageChange = (event: PageState) => {
    first.value = event.first;
    rows.value = event.rows;
};

// 홈으로 이동
const goToHome = () => {
    router.push('/');
};

// 쿼리 파라미터 변경 감시
watch(() => route.query.keyword, () => {
    performSearch();
}, { immediate: false });

// 생명주기
onMounted(async () => {
    await loadCategories();
    await performSearch();
});
</script>

<style scoped>
/* 레시피 목록·카드·개수 말풍선 스타일은 layout _recipe-card-list.scss 공통 사용 */
</style>

