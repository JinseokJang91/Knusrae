<template>
    <div class="card">
        <!-- header : 페이지 제목, 새로고침 버튼, 상세검색 다이얼로그 버튼 -->
        <!-- <div class="flex justify-between items-center mb-4">
            <h1 class="text-3xl font-bold text-gray-900">카테고리</h1>
        </div> -->

        <!-- header : 카테고리 선택 영역 (1. 검색 자동완성 + 2. 가로 탭 + 메가메뉴 패널) -->
        <div class="category-selector mb-4">
            <!-- 1. 카테고리 검색 (자동완성) -->
            <div class="category-search mb-4">
                <AutoComplete
                    v-model="categorySearchSelected"
                    :suggestions="categorySearchSuggestions"
                    @complete="onCategorySearch"
                    optionLabel="label"
                    placeholder="카테고리 검색 (예: 한식, 요리 키워드)"
                    class="category-search-input w-full"
                    @item-select="onCategorySearchSelect"
                    inputClass="w-full"
                >
                    <template #option="slotProps">
                        <div class="flex items-center gap-2 py-1">
                            <i class="pi pi-tag text-surface-500"></i>
                            <span>{{ slotProps.option.label }}</span>
                        </div>
                    </template>
                    <template #empty>
                        <div class="p-3 text-surface-500">검색 결과가 없습니다.</div>
                    </template>
                </AutoComplete>
            </div>

            <!-- 2. 가로 탭 + 메가메뉴 패널 -->
            <div class="category-tabs-panel">
                <!-- 선택 영역 라벨: 사용자가 클릭 가능한 카테고리임을 명시 -->
                <div class="category-select-label">
                    <i class="pi pi-check-circle category-select-icon" aria-hidden="true"></i>
                    <span class="category-select-title">카테고리 선택</span>
                    <span class="category-select-hint">원하는 카테고리를 클릭하세요</span>
                </div>
                <!-- 메인 카테고리 탭 (가로) -->
                <div class="main-category-tabs">
                    <button
                        v-for="mainCategory in mainCategories"
                        :key="mainCategory.codeId"
                        type="button"
                        :class="['tab-button', selectedMainCategory === mainCategory.codeId ? 'active' : '']"
                        :aria-pressed="selectedMainCategory === mainCategory.codeId"
                        :aria-label="`${mainCategory.codeName} 카테고리 선택`"
                        @click="selectMainCategory(mainCategory.codeId)"
                    >
                        {{ mainCategory.codeName }}
                    </button>
                </div>
                <!-- 선택된 메인의 상세 카테고리 패널 (맨 앞 '전체' + 칩 그리드) -->
                <div class="sub-categories-panel">
                    <div class="flex flex-wrap gap-2">
                        <button
                            v-if="selectedMainCategory"
                            key="sub-all"
                            type="button"
                            :class="['category-chip', selectedCategory === null ? 'selected' : '']"
                            :aria-pressed="selectedCategory === null"
                            aria-label="전체 카테고리 선택"
                            @click="selectCategory(null)"
                        >
                            전체
                        </button>
                        <button
                            v-for="detail in selectedMainCategoryDetails"
                            :key="detail.detailCodeId"
                            type="button"
                            :class="['category-chip', selectedCategory === detail.detailCodeId ? 'selected' : '']"
                            :aria-pressed="selectedCategory === detail.detailCodeId"
                            :aria-label="`${detail.codeName} 카테고리 선택`"
                            @click="selectCategory(detail.detailCodeId)"
                        >
                            {{ detail.codeName }}
                        </button>
                    </div>
                </div>
            </div>
        </div>

        <!-- body : 레시피 목록 섹션 -->
        <div class="recipe-section">
            <div class="flex justify-between items-center mb-3">
                <div class="flex items-center gap-3 flex-wrap">
                    <h2 class="text-2xl font-bold text-gray-900 m-0">{{ getCategoryTitle() }}</h2>
                    <div class="recipe-count-bubble">
                        <span class="text-primary font-bold">{{ totalDisplayRecipes.toLocaleString() }}</span>개의 레시피가 준비되어 있어요!
                    </div>
                    <div v-if="authStore.isLoggedIn" class="flex items-center gap-2" @mousedown.stop>
                        <span class="text-sm text-gray-600 whitespace-nowrap">팔로잉 피드 보기</span>
                        <ToggleSwitch
                            v-model="showFollowingFeed"
                            @update:modelValue="(value: boolean) => onFollowingFeedToggle(value)"
                        />
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
                loading-message="레시피를 불러오는 중..."
            />
            <PageStateBlock
                v-else-if="error"
                state="error"
                error-title="레시피를 불러올 수 없습니다"
                :error-message="error"
                retry-label="다시 시도"
                @retry="loadRecipes"
            />
            <PageStateBlock
                v-else-if="displayRecipes.length === 0"
                state="empty"
                :empty-icon="showFollowingFeed ? 'pi pi-users' : 'pi pi-book'"
                :empty-title="showFollowingFeed ? '팔로잉 피드가 비어있습니다' : '레시피가 없습니다'"
                :empty-message="showFollowingFeed ? '팔로우한 크리에이터가 없거나, 아직 레시피를 등록하지 않았습니다.' : (selectedCategory ? '선택한 카테고리에 레시피가 없습니다.' : '등록된 레시피가 없습니다.')"
            />

            <!-- 레시피 목록이 있는 경우 -->
            <template v-else>
                <div class="recipe-grid">
                    <RecipeGridCard
                        v-for="recipe in displayRecipes"
                        :key="recipe.id"
                        :recipe="recipe"
                        :category-label="recipe.primaryCategoryName || getCategoryName(recipe.category)"
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
import { getRecipeListAll, getFavorites, getFollowingFeed, toggleFavorite as toggleFavoriteApi } from '@/api/recipeApi';
import { getRecipeBooks, getBookmarksByRecipeBook } from '@/api/bookmarkApi';
import { useAuthStore } from '@/stores/authStore';
import AutoComplete from 'primevue/autocomplete';
import type { AutoCompleteCompleteEvent, AutoCompleteOptionSelectEvent } from 'primevue/autocomplete';
import SelectButton from 'primevue/selectbutton';
import Paginator from 'primevue/paginator';
import type { PageState } from 'primevue/paginator';
import PageStateBlock from '@/components/common/PageStateBlock.vue';
import RecipeGridCard from '@/components/recipe/RecipeGridCard.vue';
import BookmarkDialog from '@/components/bookmark/BookmarkDialog.vue';
import { useAppToast } from '@/utils/toast';
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import type {
    Category,
    CategoryDetail,
    CategoryRecipeItem,
    CategorySearchItem,
    MainCategory,
    SortOption,
    RecipeCategory,
    RecipeCookingTip
} from '@/types/category';
import type { Recipe as ApiRecipe } from '@/types/recipe';

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
const categories = ref<Category[]>([]);
const mainCategories = ref<MainCategory[]>([]); // 메인 카테고리 목록 (codeName 필드를 가진 항목들)
const recipes = ref<CategoryRecipeItem[]>([]);
const selectedCategory = ref<string | null>(null);
const selectedMainCategory = ref<string | null>(null); // 선택된 메인 카테고리
const searchResults = ref<CategoryRecipeItem[]>([]);
// 카테고리 검색(자동완성)용
const categorySearchSelected = ref<CategorySearchItem | null>(null);
const categorySearchSuggestions = ref<CategorySearchItem[]>([]);
const first = ref<number>(0);
const rows = ref<number>(12);
const loading = ref<boolean>(false);
const error = ref<string | null>(null);

// 쇼트컷 모드 (AppTopbar 카테고리 드롭다운에서 진입)
const shortcutMode = ref<boolean>(false);
const shortcutName = ref<string>(''); // 쇼트컷 카테고리명 (예: "한식", "고기요리")
const shortcutCodeId = ref<string>(''); // 쇼트컷 메인 카테고리 (예: "COOKING_STYLE")
const shortcutDetailIds = ref<string[]>([]); // 쇼트컷 상세 코드 배열 (예: ["1001", "1002", "1003"])

// 현재 로그인한 사용자 정보 (authStore에서 가져옴)
const currentMemberId = computed(() => authStore.memberInfo?.id || null);

// 카테고리 header > 표시된 레시피 > 개수
const filteredRecipes = computed(() => {
    let filtered = recipes.value;
    
    // 쇼트컷 모드: AppTopbar 카테고리 드롭다운에서 진입한 경우
    // 복합 필터링 적용 (여러 detailCodeId 중 하나라도 포함되면 표시)
    if (shortcutMode.value && shortcutCodeId.value && shortcutDetailIds.value.length > 0) {
        filtered = filtered.filter((recipe) => {
            if (!recipe.categoryKeys || recipe.categoryKeys.length === 0) {
                return false;
            }
            // 쇼트컷의 codeId와 detailCodeIds 조합 중 하나라도 일치하면 true
            return shortcutDetailIds.value.some((detailId) => {
                const targetKey = `${shortcutCodeId.value}-${detailId}`;
                return recipe.categoryKeys.includes(targetKey);
            });
        });
    }
    // 일반 모드: 기존 카테고리 탭/칩 선택
    else if (selectedCategory.value) {
        // 서브 카테고리가 선택된 경우 해당 서브 카테고리로 필터링
        filtered = filtered.filter((recipe) => {
            // categoryKeys 배열에 선택된 카테고리가 포함되어 있는지 확인
            // categoryKeys는 "codeId-detailCodeId" 형식의 문자열 배열
            const selectedKey = `${selectedMainCategory.value}-${selectedCategory.value}`;
            return recipe.categoryKeys && recipe.categoryKeys.includes(selectedKey);
        });
    }
    // 메인 카테고리만 선택되고 서브 카테고리가 선택되지 않은 경우, 해당 메인 카테고리의 모든 서브 카테고리로 필터링
    else if (selectedMainCategory.value) {
        filtered = filtered.filter((recipe) => {
            // categoryKeys 중에서 선택된 메인 카테고리로 시작하는 항목이 있는지 확인
            return recipe.categoryKeys && recipe.categoryKeys.some(key => key.startsWith(`${selectedMainCategory.value}-`));
        });
    }
    
    return filtered;
});

// 정렬 옵션 (최신순, 조회순, 댓글순)
const SORT_LATEST = 'latest' as const;
const SORT_HITS = 'hits' as const;
const SORT_COMMENTS = 'comments' as const;
const sortOptions: SortOption[] = [
    { label: '최신순', value: SORT_LATEST },
    { label: '조회순', value: SORT_HITS },
    { label: '댓글순', value: SORT_COMMENTS }
];
const sortBy = ref<string>(SORT_LATEST);

// 팔로잉 피드 보기 토글 (로그인 사용자만 노출)
const showFollowingFeed = ref<boolean>(false);
const followingFeedRecipes = ref<CategoryRecipeItem[]>([]);

// 팔로잉 피드에 카테고리 필터 적용 (filteredRecipes와 동일한 로직)
const filteredFollowingFeedRecipes = computed(() => {
    let filtered = followingFeedRecipes.value;
    if (shortcutMode.value && shortcutCodeId.value && shortcutDetailIds.value.length > 0) {
        filtered = filtered.filter((recipe) => {
            if (!recipe.categoryKeys || recipe.categoryKeys.length === 0) return false;
            return shortcutDetailIds.value.some((detailId) => {
                const targetKey = `${shortcutCodeId.value}-${detailId}`;
                return recipe.categoryKeys.includes(targetKey);
            });
        });
    } else if (selectedCategory.value) {
        const selectedKey = `${selectedMainCategory.value}-${selectedCategory.value}`;
        filtered = filtered.filter(
            (recipe) => recipe.categoryKeys && recipe.categoryKeys.includes(selectedKey)
        );
    } else if (selectedMainCategory.value) {
        filtered = filtered.filter(
            (recipe) =>
                recipe.categoryKeys &&
                recipe.categoryKeys.some((key) => key.startsWith(`${selectedMainCategory.value}-`))
        );
    }
    return filtered;
});

// 정렬/표시용 소스: 팔로잉 피드 모드면 카테고리 필터 적용된 팔로잉 목록, 아니면 기존 카테고리/검색 목록
const listSourceForSort = computed(() => {
    if (showFollowingFeed.value) {
        return filteredFollowingFeedRecipes.value;
    }
    return searchResults.value.length > 0 ? searchResults.value : filteredRecipes.value;
});

// 정렬된 레시피 목록 (표시용 소스)
const sortedRecipes = computed(() => {
    const source = listSourceForSort.value;
    const list = [...source];
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

// 카테고리 body > 레시피 (정렬된 목록에서 페이지 슬라이스)
const displayRecipes = computed(() => {
    return sortedRecipes.value.slice(first.value, first.value + rows.value);
});

// 카테고리 footer > 페이징 처리 (정렬된 목록 개수)
const totalDisplayRecipes = computed(() => sortedRecipes.value.length);

// 선택된 메인 카테고리의 서브 카테고리 목록 (details)
const selectedMainCategoryDetails = computed(() => {
    if (!selectedMainCategory.value) {
        return [];
    }
    const mainCategory = mainCategories.value.find((cat) => cat.codeId === selectedMainCategory.value);
    return mainCategory?.details || [];
});

// 검색용 플랫 카테고리 목록 (메인 전체 + 메인별 상세)
const allCategoriesFlat = computed((): CategorySearchItem[] => {
    const list: CategorySearchItem[] = [];
    mainCategories.value.forEach((main) => {
        list.push({ label: `${main.codeName} (전체)`, mainCodeId: main.codeId, detailCodeId: null });
        (main.details || []).forEach((d) => {
            list.push({ label: `${main.codeName} > ${d.codeName}`, mainCodeId: main.codeId, detailCodeId: d.detailCodeId });
        });
    });
    return list;
});

// Function > onMounted > 카테고리 조회
const loadCategories = async (): Promise<void> => {
    try {
        const codes = (await getCommonCodesByGroup('CATEGORY')) as MainCategory[];

        // 메인 카테고리 목록 저장 (codeName 필드를 가진 모든 항목들)
        mainCategories.value = codes.map((code: MainCategory) => ({
            codeId: code.codeId,
            codeName: code.codeName,
            details: code.details ?? []
        }));

        // 모든 메인 카테고리의 서브 카테고리를 categories 배열에 저장
        categories.value = [];
        codes.forEach((code: MainCategory) => {
            if (Array.isArray(code.details)) {
                code.details.forEach((detail: CategoryDetail) => {
                    categories.value.push({
                        value: detail.detailCodeId,
                        name: detail.codeName,
                        description: `${detail.codeName} 관련 레시피`,
                        icon: 'pi pi-tag',
                        color: '#3B82F6',
                        recipeCount: 0,
                        mainCategoryId: code.codeId // 메인 카테고리 ID 추가
                    });
                });
            }
        });

        // 메인 카테고리 기본값: 미선택(전체 레시피)
    } catch (err) {
        // 기본값 설정
        mainCategories.value = [
            {
                codeId: 'COOKING_KEYWORD',
                codeName: '요리 키워드',
                details: []
            }
        ];
        categories.value = [
            {
                value: '1001',
                name: '한식',
                description: '전통 한국 요리',
                icon: 'pi pi-home',
                color: '#3B82F6',
                recipeCount: 0
            }
        ];
    }
};

// Function > 팔로잉 피드 보기 토글
const onFollowingFeedToggle = async (value: boolean): Promise<void> => {
    first.value = 0;
    if (value) {
        loading.value = true;
        try {
            await loadFollowingFeed();
        } finally {
            loading.value = false;
        }
    } else {
        followingFeedRecipes.value = [];
    }
};

// Function > 팔로잉 피드 레시피 조회 (API Recipe → CategoryRecipeItem 매핑)
const loadFollowingFeed = async (): Promise<void> => {
    if (!currentMemberId.value) {
        followingFeedRecipes.value = [];
        return;
    }
    try {
        const feedRecipes = await getFollowingFeed(0, 100);
        let favoriteRecipeIds: number[] = [];
        try {
            const favoritesList = await getFavorites(currentMemberId.value);
            favoriteRecipeIds = favoritesList.map((fav) => fav.recipeId);
        } catch {
            // 무시
        }
        followingFeedRecipes.value = (feedRecipes as ApiRecipe[]).map((recipe: ApiRecipe) => {
            const cookingTime = extractCookingTime(recipe.cookingTips);
            const servings = extractServings(recipe.cookingTips);
            const isFavorite = favoriteRecipeIds.includes(recipe.id);
            const categoryKeys = extractCategoryKeys(recipe);
            const categoryIds = extractCategoryIds(recipe);
            const firstCategory = recipe.categories?.[0];
            const primaryCategoryId = firstCategory?.detailCodeId || categoryIds[0] || null;
            const primaryCategoryName = firstCategory?.detailName || firstCategory?.codeName || null;
            return {
                ...recipe,
                categoryKeys,
                categoryIds,
                category: primaryCategoryId,
                primaryCategoryName,
                cookingTime,
                servings,
                isFavorite,
                commentCount: recipe.commentCount ?? 0
            } as CategoryRecipeItem;
        });
    } catch (err) {
        console.error('팔로잉 피드 로드 실패:', err);
        followingFeedRecipes.value = [];
    }
};

// Function > onMounted > 레시피 조회
// 레시피의 모든 카테고리 키(codeId-detailCodeId)들을 추출하는 함수
const extractCategoryKeys = (recipe: { categories?: RecipeCategory[] }): string[] => {
    if (!recipe || !recipe.categories || recipe.categories.length === 0) {
        return [];
    }
    // 모든 카테고리의 "codeId-detailCodeId" 형식 문자열을 배열로 반환
    return recipe.categories
        .filter((category) => category.codeId && category.detailCodeId)
        .map((category) => `${category.codeId}-${category.detailCodeId}`);
};

// 레시피의 모든 카테고리 detailCodeId들을 추출하는 함수 (표시용)
const extractCategoryIds = (recipe: { categories?: RecipeCategory[] }): string[] => {
    if (!recipe || !recipe.categories || recipe.categories.length === 0) {
        return [];
    }
    // 모든 카테고리의 detailCodeId를 배열로 반환
    return recipe.categories.map((category) => category.detailCodeId || category.codeId).filter(Boolean);
};

// Function > cookingTips에서 요리 시간 추출
const extractCookingTime = (cookingTips: RecipeCookingTip[] | undefined): string | null => {
    if (!cookingTips || !Array.isArray(cookingTips)) {
        return null;
    }
    const cookingTimeTip = cookingTips.find((tip) => tip.codeId === 'COOKING_TIME');
    return cookingTimeTip?.detailName || null;
};

// Function > cookingTips에서 인분 수 추출
const extractServings = (cookingTips: RecipeCookingTip[] | undefined): string | null => {
    if (!cookingTips || !Array.isArray(cookingTips)) {
        return null;
    }
    const servingTip = cookingTips.find((tip) => tip.codeId === 'SERVINGS');
    return servingTip?.detailName || null;
};

const loadRecipes = async (): Promise<void> => {
    try {
        loading.value = true;
        error.value = null;

        const data = await getRecipeListAll();

        let favoriteRecipeIds: number[] = [];
        if (currentMemberId.value) {
            try {
                const favoritesList = await getFavorites(currentMemberId.value);
                favoriteRecipeIds = favoritesList.map((fav) => fav.recipeId);
            } catch (err) {
                console.error('찜 목록을 가져올 수 없습니다:', err);
            }
        }

        recipes.value = (data as ApiRecipe[]).map((recipe: ApiRecipe) => {
            // cookingTips에서 SERVING과 COOKING_TIME 추출
            const cookingTime = extractCookingTime(recipe.cookingTips);
            const servings = extractServings(recipe.cookingTips);
            

            const isFavorite = favoriteRecipeIds.includes(recipe.id);
            
            // 레시피의 모든 카테고리 키(codeId-detailCodeId) 추출
            const categoryKeys = extractCategoryKeys(recipe);
            // 레시피의 모든 카테고리 ID 추출 (표시용)
            const categoryIds = extractCategoryIds(recipe);
            // 표시용 대표 카테고리: 첫 번째 카테고리 (7개 중 최소 1개만 있을 수 있음)
            const firstCategory = recipe.categories?.[0];
            const primaryCategoryId = firstCategory?.detailCodeId || categoryIds[0] || null;
            const primaryCategoryName = firstCategory?.detailName || firstCategory?.codeName || null;

            return {
                ...recipe,
                categoryKeys, // 모든 카테고리 키 배열 (필터링용, "codeId-detailCodeId" 형식)
                categoryIds, // 모든 카테고리 ID 배열 (표시용)
                category: primaryCategoryId, // 표시용 대표 카테고리 ID (필터/폴백용)
                primaryCategoryName, // 표시용 대표 카테고리명 (API 기준, 새 7개 카테고리 대응)
                cookingTime,
                servings,
                isFavorite,
                commentCount: recipe.commentCount ?? 0
            } as CategoryRecipeItem;
        });

        // 북마크된 레시피 ID 목록 로드 (카드 선택 상태 표시용)
        await loadBookmarkedRecipeIds();
    } catch (err: unknown) {
        console.error('레시피 로드 실패:', err);
        error.value = err instanceof Error ? err.message : '레시피를 불러오는데 실패했습니다.';

        // API 실패 시 더미 데이터 사용 (primaryCategoryName으로 카드 태그 표시)
        const fallbackRecipe = (r: Omit<CategoryRecipeItem, 'status' | 'visibility' | 'memberId'>): CategoryRecipeItem => ({
            ...r,
            status: 'PUBLISHED',
            visibility: 'PUBLIC',
            memberId: 0
        });
        recipes.value = [
            fallbackRecipe({ id: 1, title: '김치찌개', thumbnail: 'https://images.unsplash.com/photo-1565299624946-b28f40a0ca4b?w=400', category: '1001', categoryIds: ['1001'], categoryKeys: ['COOKING_STYLE-1001'], primaryCategoryName: '한식', cookingTime: '30분', servings: '2인분', hits: 1250, isFavorite: false, commentCount: 173 }),
            fallbackRecipe({ id: 2, title: '짜장면', thumbnail: 'https://images.unsplash.com/photo-1563379091339-03246963d4d8?w=400', category: '1003', categoryIds: ['1003'], categoryKeys: ['COOKING_STYLE-1003'], primaryCategoryName: '중식', cookingTime: '20분', servings: '2인분', hits: 980, isFavorite: false, commentCount: 45 }),
            fallbackRecipe({ id: 3, title: '초밥', thumbnail: 'https://images.unsplash.com/photo-1579584425555-c3ce17fd4351?w=400', category: '1004', categoryIds: ['1004'], categoryKeys: ['COOKING_STYLE-1004'], primaryCategoryName: '일식', cookingTime: '45분', servings: '4인분', hits: 2100, isFavorite: false, commentCount: 1435 }),
            fallbackRecipe({ id: 4, title: '파스타', thumbnail: 'https://images.unsplash.com/photo-1621996346565-e3dbc353d2e5?w=400', category: '1005', categoryIds: ['1005'], categoryKeys: ['COOKING_STYLE-1005'], primaryCategoryName: '이탈리안', cookingTime: '25분', servings: '2인분', hits: 1560, isFavorite: true, commentCount: 0 }),
            fallbackRecipe({ id: 5, title: '치즈케이크', thumbnail: 'https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=400', category: '1001', categoryIds: ['1001'], categoryKeys: ['COOKING_DESSERT-1001'], primaryCategoryName: '쿠키', cookingTime: '90분', servings: '8인분', hits: 890, isFavorite: true, commentCount: 12 }),
            fallbackRecipe({ id: 6, title: '불고기', thumbnail: 'https://images.unsplash.com/photo-1529042410759-befb1204b468?w=400', category: '1001', categoryIds: ['1001'], categoryKeys: ['COOKING_STYLE-1001'], primaryCategoryName: '한식', cookingTime: '15분', servings: '3인분', hits: 1750, isFavorite: false, commentCount: 89 })
        ];
    } finally {
        loading.value = false;
    }
};

// Function > Button > 메인 카테고리 선택
const selectMainCategory = (codeId: string): void => {
    // 쇼트컷 모드 해제 (사용자가 메인 카테고리 탭을 클릭하면 일반 모드로 전환)
    clearShortcutMode();
    
    // 이미 선택된 메인을 다시 클릭하면 선택 해제(전체 레시피로)
    if (selectedMainCategory.value === codeId) {
        selectedMainCategory.value = null;
        selectedCategory.value = null;
    } else {
        selectedMainCategory.value = codeId;
        selectedCategory.value = null;
    }
    searchResults.value = [];
    first.value = 0;
};

// 정렬 변경 시 첫 페이지로
const onSortChange = (): void => {
    first.value = 0;
};

// Function > Button > body 카테고리 선택 시 목록 필터링
const selectCategory = (categoryValue: string | null): void => {
    // 쇼트컷 모드 해제 (사용자가 서브 카테고리를 선택하면 일반 모드로 전환)
    clearShortcutMode();
    
    selectedCategory.value = selectedCategory.value === categoryValue ? null : categoryValue;
    
    searchResults.value = [];
    first.value = 0;
};

// 카테고리 검색(자동완성): 입력 시 제안 목록 필터링
const onCategorySearch = (event: AutoCompleteCompleteEvent): void => {
    const query = (event.query || '').trim().toLowerCase();
    if (!query) {
        // 포커스만 했을 때는 메인 카테고리(전체)만 표시
        categorySearchSuggestions.value = allCategoriesFlat.value.filter((item) => item.detailCodeId === null);
        return;
    }
    categorySearchSuggestions.value = allCategoriesFlat.value.filter(
        (item) => item.label.toLowerCase().includes(query)
    );
};

// 카테고리 검색(자동완성): 항목 선택 시 메인/상세 적용 후 검색창 초기화
const onCategorySearchSelect = (event: AutoCompleteOptionSelectEvent): void => {
    // 쇼트컷 모드 해제 (카테고리 검색에서 선택하면 일반 모드로 전환)
    clearShortcutMode();
    
    const item = event.value as CategorySearchItem;
    if (!item) return;
    selectedMainCategory.value = item.mainCodeId;
    selectedCategory.value = item.detailCodeId ?? null;
    categorySearchSelected.value = null;
    searchResults.value = [];
    first.value = 0;
};

// const viewCategory = (categoryValue: string): void => {
//     selectedCategory.value = categoryValue;
//     searchResults.value = [];
//     first.value = 0;
// };

// Function > 대표 카테고리명 조회 (detailCodeId로 서브 카테고리 목록에서 이름 찾기, 폴백용)
const getCategoryName = (categoryValue: string | null | undefined): string | null => {
    if (!categoryValue) return null;
    // 서브 카테고리 목록에서 value(detailCodeId)가 일치하는 첫 번째 항목의 이름 반환
    const category = categories.value.find((cat) => cat.value === categoryValue);
    return category ? category.name : null;
};

// Function > header 타이틀 생성 (메인 카테고리 + 서브 카테고리)
const getCategoryTitle = (): string => {
    // 팔로잉 피드 보기 모드
    if (showFollowingFeed.value) {
        return '팔로잉 피드';
    }
    // 쇼트컷 모드: AppTopbar 카테고리 드롭다운에서 진입한 경우
    if (shortcutMode.value && shortcutName.value) {
        return shortcutName.value;
    }
    
    // 아무것도 선택되지 않은 경우
    if (!selectedMainCategory.value) {
        return '전체 레시피';
    }
    
    // 서브 카테고리가 선택되지 않은 경우: 전체 레시피로 표시
    if (!selectedCategory.value) {
        return '전체 레시피';
    }
    
    // 서브 카테고리가 선택된 경우: 메인 > 서브 형식으로 표시
    // 메인 카테고리 이름 찾기
    const mainCategory = mainCategories.value.find((cat) => cat.codeId === selectedMainCategory.value);
    const mainCategoryName = mainCategory ? mainCategory.codeName : selectedMainCategory.value;
    
    // selectedMainCategoryDetails에서 서브 카테고리 찾기
    const subCategory = selectedMainCategoryDetails.value.find((detail) => detail.detailCodeId === selectedCategory.value);
    const subCategoryName = subCategory ? subCategory.codeName : selectedCategory.value;
    return `${mainCategoryName} > ${subCategoryName}`;
};

// Function > 레시피 상세 페이지 댓글 영역으로 이동
const scrollToComments = (recipeId: number): void => {
    router.push(`/recipe/${recipeId}#comments`);
};

// Function > Button > 찜 목록 추가
const toggleFavorite = async (recipeId: number): Promise<void> => {
    // 로그인 확인
    if (!currentMemberId.value) {
        return;
    }

    const list = showFollowingFeed.value ? followingFeedRecipes.value : recipes.value;
    const recipe = list.find((r) => r.id === recipeId);
    if (!recipe) return;

    try {
        const response = await toggleFavoriteApi(currentMemberId.value, recipeId);
        recipe.isFavorite = response.isFavorite;
    } catch (err) {
        console.error('찜 토글 실패:', err);
    }
};

// Function > 레시피 상세 조회
const viewRecipe = (recipeId: number): void => {
    router.push(`/recipe/${recipeId}`);
};

// Function > Button > 북마크 Dialog 열기
const bookmarkRecipe = (recipeId: number): void => {
    if (!currentMemberId.value) {
        showWarn('로그인이 필요한 기능입니다.');
        router.push({ path: '/auth/login', query: { redirect: route.fullPath } });
        return;
    }
    bookmarkRecipeId.value = recipeId;
    bookmarkDialogVisible.value = true;
};

const onBookmarked = async (): Promise<void> => {
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
        const recipeBooks = await getRecipeBooks();
        const ids = new Set<number>();
        for (const recipeBook of recipeBooks) {
            const bookmarks = await getBookmarksByRecipeBook(recipeBook.id);
            bookmarks.forEach((b) => ids.add(b.recipeId));
        }
        bookmarkedRecipeIds.value = ids;
    } catch {
        bookmarkedRecipeIds.value = new Set();
    }
};

// Function > 페이징
const onPageChange = (event: PageState): void => {
    first.value = event.first;
    rows.value = event.rows;
};

// 카테고리 선택 감시
watch(selectedCategory, (_newCategory: string | null, _oldCategory: string | null) => {
    first.value = 0; // 페이지 초기화
});

// 쇼트컷 모드 해제 (사용자가 메인 카테고리 탭을 클릭하면 일반 모드로 전환)
const clearShortcutMode = (): void => {
    if (shortcutMode.value) {
        shortcutMode.value = false;
        shortcutName.value = '';
        shortcutCodeId.value = '';
        shortcutDetailIds.value = [];
        
        // URL에서 쇼트컷 파라미터 제거
        router.replace({ path: '/recipe/category', query: {} });
    }
};

// 쿼리 파라미터 처리 (쇼트컷 모드 설정)
const handleQueryParams = (): void => {
    const { shortcut, codeId, details, name } = route.query;
    
    if (shortcut && codeId && details && name) {
        // 쇼트컷 모드 활성화
        shortcutMode.value = true;
        shortcutName.value = String(name);
        shortcutCodeId.value = String(codeId);
        shortcutDetailIds.value = String(details).split(',').filter(Boolean);
        
        // 기존 카테고리 선택 초기화
        selectedMainCategory.value = null;
        selectedCategory.value = null;
        first.value = 0;
    } else {
        // 쇼트컷 모드 비활성화 (일반 카테고리 탐색)
        shortcutMode.value = false;
        shortcutName.value = '';
        shortcutCodeId.value = '';
        shortcutDetailIds.value = [];
    }
};

// 쿼리 파라미터 변경 감시 (페이지 내에서 카테고리 변경 시)
watch(() => route.query, () => {
    handleQueryParams();
}, { deep: true });

// 생명주기
onMounted(() => {
    // 쿼리 파라미터 처리 (쇼트컷 모드 확인)
    handleQueryParams();
    
    // 로그인 여부와 관계없이 카테고리 및 레시피 조회
    const initializeData = async (): Promise<void> => {
        await loadCategories();
        await loadRecipes();
    };
    initializeData();
});
</script>

<style scoped>
.stat-card {
    background: linear-gradient(135deg, #ea580c 0%, #fed7aa 100%);
    color: white;
}

.stat-card .text-900 {
    color: white !important;
}

.stat-card .text-600 {
    color: rgba(255, 255, 255, 0.8) !important;
}

.category-card {
    transition:
        transform 0.2s,
        box-shadow 0.2s;
}

.category-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.category-card.selected {
    border: 2px solid var(--primary-color);
    box-shadow: 0 0 0 0.2rem rgba(var(--primary-color-rgb), 0.25);
}

.category-icon {
    font-size: 3rem;
}

/* 정렬 버튼: 오렌지 톤 테마 (PrimeVue 4 SelectButton = ToggleButton, .p-togglebutton / .p-togglebutton-checked) */
.recipe-sort-buttons :deep(.p-selectbutton .p-togglebutton) {
    border-color: #fdba74;
    color: #c2410c;
    background: #fff7ed;
}
.recipe-sort-buttons :deep(.p-selectbutton .p-togglebutton:hover) {
    border-color: #fb923c;
    color: #9a3412;
    background: #ffedd5;
}
.recipe-sort-buttons :deep(.p-selectbutton .p-togglebutton.p-togglebutton-checked) {
    border-color: #ea580c;
    background: #f97316;
    color: #fff;
}
.recipe-sort-buttons :deep(.p-selectbutton .p-togglebutton.p-togglebutton-checked:hover) {
    background: #ea580c;
    border-color: #c2410c;
    color: #fff;
}

/* 카테고리 선택기 스타일 */
.category-selector {
    background: var(--surface-card);
    border: 1px solid var(--surface-border);
    border-radius: 12px;
    padding: 1.5rem;
    margin-bottom: 2rem;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

/* 1. 카테고리 검색(자동완성) */
.category-search .category-search-input {
    max-width: 400px;
}

/* 2. 가로 탭 + 메가메뉴 패널 */
.category-tabs-panel {
    border-top: 1px solid var(--surface-border);
    padding-top: 1rem;
}

/* 카테고리 선택 영역 라벨 (선택 가능함을 명시) */
.category-select-label {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 0.5rem 1rem;
    margin-bottom: 0.75rem;
    padding: 0.5rem 0;
}

.category-select-icon {
    color: var(--primary-color, #1f2937);
    font-size: 1.1rem;
}

.category-select-title {
    font-weight: 600;
    font-size: 0.95rem;
    color: var(--text-color);
}

.category-select-hint {
    font-size: 0.8rem;
    color: var(--text-color-secondary);
}

.main-category-tabs {
    display: flex;
    flex-wrap: wrap;
    gap: 0.5rem;
    margin-bottom: 1rem;
}

.main-category-tabs .tab-button {
    padding: 0.6rem 1rem;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.2s ease;
    font-weight: 600;
    font-size: 0.9rem;
    color: #374151;
    background: #f3f4f6;
    border: 1px solid #e5e7eb;
}

.main-category-tabs .tab-button:hover {
    background: #e5e7eb;
    border-color: #d1d5db;
    color: #111827;
}

.main-category-tabs .tab-button.active {
    background: var(--primary-color, #1f2937);
    color: white;
    border-color: var(--primary-color, #1f2937);
}

.sub-categories-panel {
    padding: 0.75rem 0;
    min-height: 52px;
}

.category-chip {
    padding: 0.5rem 0.9rem;
    border-radius: 9999px;
    cursor: pointer;
    transition: all 0.2s ease;
    font-weight: 500;
    font-size: 0.875rem;
    color: #374151;
    background: #f9fafb;
    border: 1px solid #e5e7eb;
}

.category-chip:hover {
    background: #f3f4f6;
    border-color: #d1d5db;
    color: #111827;
}

.category-chip.selected {
    background: var(--primary-color, #1f2937);
    color: white;
    border-color: var(--primary-color, #1f2937);
}

/* 반응형 디자인 (레시피 그리드/카드는 _recipe-card-list.scss 공통) */
@media (max-width: 768px) {
    .category-selector {
        padding: 1rem;
    }

    .category-search .category-search-input {
        max-width: 100%;
    }

    .main-category-tabs {
        gap: 0.35rem;
    }

    .main-category-tabs .tab-button {
        padding: 0.5rem 0.75rem;
        font-size: 0.85rem;
    }

    .category-chip {
        padding: 0.4rem 0.75rem;
        font-size: 0.8rem;
    }
}
</style>
