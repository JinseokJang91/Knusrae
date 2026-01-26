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
                <h2 class="text-2xl font-semibold text-gray-900 m-0">
                    검색 결과 ({{ totalDisplayRecipes }})
                </h2>
                <div class="flex gap-2">
                    <Button icon="pi pi-th-large" :class="viewMode === 'grid' ? 'p-button-primary' : 'p-button-secondary'" size="small" @click="viewMode = 'grid'" />
                    <Button icon="pi pi-list" :class="viewMode === 'list' ? 'p-button-primary' : 'p-button-secondary'" size="small" @click="viewMode = 'list'" />
                </div>
            </div>

            <!-- 로딩 상태 -->
            <div v-if="loading" class="text-center py-8">
                <ProgressSpinner />
                <p class="text-gray-600 mt-3">검색 중...</p>
            </div>

            <!-- 에러 상태 -->
            <div v-else-if="error" class="text-center py-8">
                <i class="pi pi-exclamation-triangle text-6xl text-red-500 mb-4"></i>
                <h3 class="text-2xl font-semibold text-gray-600 mb-2">검색 중 오류가 발생했습니다</h3>
                <p class="text-gray-600 mb-4">{{ error }}</p>
                <Button label="다시 시도" @click="performSearch" />
            </div>

            <!-- 검색 결과가 있는 경우 -->
            <div v-else-if="displayRecipes.length > 0">
                <!-- 그리드 뷰 (카드 형태) -->
                <div v-if="viewMode === 'grid'" class="recipe-grid">
                    <div v-for="recipe in displayRecipes" :key="recipe.id" class="recipe-card-wrapper" @click="viewRecipe(recipe.id)">
                        <Card class="recipe-card h-full">
                            <template #header>
                                <div class="recipe-image-container">
                                    <img :src="recipe.thumbnail || '/placeholder-recipe.jpg'" :alt="recipe.title" class="recipe-image" />
                                    <div class="recipe-overlay">
                                        <div class="recipe-actions">
                                            <Button :icon="recipe.isFavorite ? 'pi pi-heart-fill' : 'pi pi-heart'" :class="recipe.isFavorite ? 'p-button-danger' : 'p-button-secondary'" size="large" rounded @click.stop="toggleFavorite(recipe.id)" />
                                            <Button icon="pi pi-bookmark" severity="secondary" size="large" rounded @click.stop="bookmarkRecipe(recipe.id)" />
                                        </div>
                                        <Tag v-if="getCategoryName(recipe.category)" :value="getCategoryName(recipe.category)" severity="info" class="recipe-category-tag" />
                                    </div>
                                    <!-- 조회수 표시 (이미지 우측 하단) -->
                                    <div v-if="formatCount(recipe.hits)" class="recipe-hits-overlay">
                                        조회수 {{ formatCount(recipe.hits) }}
                                    </div>
                                </div>
                            </template>
                            <template #content>
                                <div class="recipe-content">
                                    <h3 class="recipe-title">
                                        <template v-for="(part, index) in highlightKeyword(recipe.title)" :key="index">
                                            <mark v-if="part.isHighlight" class="bg-yellow-200">{{ part.text }}</mark>
                                            <span v-else>{{ part.text }}</span>
                                        </template>
                                    </h3>
                                    <div class="recipe-meta">
                                        <div class="recipe-info">
                                            <div v-if="recipe.cookingTime" class="info-item">
                                                <i class="pi pi-clock"></i>
                                                <span>{{ recipe.cookingTime }}</span>
                                            </div>
                                            <div v-if="recipe.servings" class="info-item">
                                                <i class="pi pi-users"></i>
                                                <span>{{ recipe.servings }}</span>
                                            </div>
                                        </div>
                                        <div v-if="recipe.memberNickname || recipe.memberName" class="recipe-author mt-2 flex items-center gap-2">
                                            <div class="w-6 h-6 rounded-full bg-gray-300 flex items-center justify-center overflow-hidden flex-shrink-0">
                                                <img 
                                                    v-if="recipe.memberProfileImage" 
                                                    :src="recipe.memberProfileImage" 
                                                    alt="작성자 프로필" 
                                                    class="w-full h-full object-cover"
                                                />
                                                <i v-else class="pi pi-user text-gray-600 text-xs"></i>
                                            </div>
                                            <span class="text-sm text-gray-600">{{ recipe.memberNickname || recipe.memberName }}</span>
                                        </div>
                                        <!-- 댓글 개수 표시 -->
                                        <div v-if="formatCount(recipe.commentCount)" class="recipe-comment-count mt-1">
                                            <span class="text-sm text-gray-600 cursor-pointer hover:text-primary" @click.stop="scrollToComments(recipe.id)">
                                                댓글 {{ formatCount(recipe.commentCount) }}
                                            </span>
                                        </div>
                                    </div>
                                </div>
                            </template>
                        </Card>
                    </div>
                </div>

                <!-- 리스트 뷰 -->
                <div v-else class="recipe-list">
                    <div v-for="recipe in displayRecipes" :key="recipe.id" class="recipe-list-item">
                        <div class="flex items-center gap-3 p-3 rounded hover:bg-gray-50 transition-colors duration-150">
                            <img :src="recipe.thumbnail || '/placeholder-recipe.jpg'" :alt="recipe.title" class="recipe-thumbnail" />
                            <div class="flex-1">
                                <h4 class="text-lg font-semibold text-gray-900 m-0 mb-1">
                                    <template v-for="(part, index) in highlightKeyword(recipe.title)" :key="index">
                                        <mark v-if="part.isHighlight" class="bg-yellow-200">{{ part.text }}</mark>
                                        <span v-else>{{ part.text }}</span>
                                    </template>
                                </h4>
                                <div class="flex items-center gap-3 text-sm text-gray-500">
                                    <div v-if="recipe.cookingTime" class="flex items-center gap-1">
                                        <i class="pi pi-clock"></i>
                                        <span>{{ recipe.cookingTime }}</span>
                                    </div>
                                    <div v-if="recipe.servings" class="flex items-center gap-1">
                                        <i class="pi pi-users"></i>
                                        <span>{{ recipe.servings }}</span>
                                    </div>
                                    <Tag v-if="getCategoryName(recipe.category)" :value="getCategoryName(recipe.category)" severity="info" />
                                    <!-- 조회수 표시 -->
                                    <span v-if="formatCount(recipe.hits)" class="text-sm text-gray-600">
                                        조회수 {{ formatCount(recipe.hits) }}
                                    </span>
                                    <!-- 댓글 개수 표시 -->
                                    <span v-if="formatCount(recipe.commentCount)" class="text-sm text-gray-600 cursor-pointer hover:text-primary" @click.stop="scrollToComments(recipe.id)">
                                        댓글 {{ formatCount(recipe.commentCount) }}
                                    </span>
                                </div>
                            </div>
                            <div class="flex items-center gap-3">
                                <div v-if="recipe.memberNickname || recipe.memberName" class="flex items-center gap-2">
                                    <div class="w-6 h-6 rounded-full bg-gray-300 flex items-center justify-center overflow-hidden flex-shrink-0">
                                        <img 
                                            v-if="recipe.memberProfileImage" 
                                            :src="recipe.memberProfileImage" 
                                            alt="작성자 프로필" 
                                            class="w-full h-full object-cover"
                                        />
                                        <i v-else class="pi pi-user text-gray-600 text-xs"></i>
                                    </div>
                                    <span class="text-sm text-gray-600">{{ recipe.memberNickname || recipe.memberName }}</span>
                                </div>
                                <div class="flex flex-col gap-2">
                                    <Button :icon="recipe.isFavorite ? 'pi pi-heart-fill' : 'pi pi-heart'" :class="recipe.isFavorite ? 'p-button-danger' : 'p-button-secondary'" size="small" rounded @click="toggleFavorite(recipe.id)" />
                                    <Button label="상세보기" size="small" @click="viewRecipe(recipe.id)" />
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- footer : 페이지네이션 -->
                <div class="flex justify-center mt-4">
                    <Paginator v-model:first="first" :rows="rows" :totalRecords="totalDisplayRecipes" @page="onPageChange" template="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink" />
                </div>
            </div>

            <!-- 빈 상태 -->
            <div v-else class="text-center py-8">
                <i class="pi pi-search text-6xl text-300 mb-4"></i>
                <h3 class="text-2xl font-semibold text-gray-600 mb-2">검색 결과가 없습니다</h3>
                <p class="text-gray-600 mb-4">
                    <span v-if="searchKeyword">"{{ searchKeyword }}"</span>
                    <span v-else>검색어를 입력해주세요.</span>
                    에 대한 검색 결과가 없습니다.
                </p>
                <Button label="다시 검색" @click="goToHome" />
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { httpJson } from '@/utils/http';
import { useAuthStore } from '@/stores/authStore';
import { searchRecipes } from '@/utils/search';
import Button from 'primevue/button';
import Card from 'primevue/card';
import Paginator from 'primevue/paginator';
import ProgressSpinner from 'primevue/progressspinner';
import Tag from 'primevue/tag';
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getApiBaseUrl } from '@/utils/constants';
import type { Recipe } from '@/types/recipe';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

// 반응형 데이터
const recipes = ref<Recipe[]>([]);
const viewMode = ref('grid');
const first = ref(0);
const rows = ref(12);
const loading = ref(false);
const error = ref<string | null>(null);
const searchKeyword = ref<string>('');

// 현재 로그인한 사용자 정보
const currentMemberId = computed(() => authStore.memberInfo?.id || null);

// API 기본 URL
const API_BASE_URL = getApiBaseUrl('cook');

// 카테고리 목록 (카테고리 이름 표시용)
const categories = ref<Array<{ value: string; name: string; mainCategoryId?: string }>>([]);

// 검색 결과 표시
const displayRecipes = computed(() => {
    return recipes.value.slice(first.value, first.value + rows.value);
});

// 전체 검색 결과 개수
const totalDisplayRecipes = computed(() => {
    return recipes.value.length;
});

// 카테고리 이름 가져오기
const getCategoryName = (categoryValue: string | null | undefined) => {
    if (!categoryValue) return null;
    const category = categories.value.find((cat) => cat.value === categoryValue && cat.mainCategoryId === 'COOKING_KEYWORD');
    return category ? category.name : null;
};

// 검색어 하이라이트 (안전한 방식: 배열 반환)
const highlightKeyword = (text: string): Array<{ text: string; isHighlight: boolean }> => {
    if (!searchKeyword.value || !text) return [{ text, isHighlight: false }];
    
    const keyword = searchKeyword.value;
    const regex = new RegExp(`(${keyword.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')})`, 'gi');
    const parts: Array<{ text: string; isHighlight: boolean }> = [];
    let lastIndex = 0;
    let match;
    
    while ((match = regex.exec(text)) !== null) {
        // 매치 이전 텍스트 추가
        if (match.index > lastIndex) {
            parts.push({ text: text.substring(lastIndex, match.index), isHighlight: false });
        }
        // 매치된 텍스트 추가 (하이라이트)
        parts.push({ text: match[0], isHighlight: true });
        lastIndex = regex.lastIndex;
    }
    
    // 남은 텍스트 추가
    if (lastIndex < text.length) {
        parts.push({ text: text.substring(lastIndex), isHighlight: false });
    }
    
    return parts.length > 0 ? parts : [{ text, isHighlight: false }];
};

// 카테고리 목록 로드 (카테고리 이름 표시용)
const loadCategories = async () => {
    try {
        const response = await httpJson(API_BASE_URL, '/api/common-codes?codeGroup=CATEGORY', {
            method: 'GET'
        });

        const codes = Array.isArray(response) ? response : [];
        
        categories.value = [];
        codes.forEach((code: { codeId: string; details?: Array<{ detailCodeId: string; codeName: string }> }) => {
            if (Array.isArray(code.details)) {
                code.details.forEach((detail: { detailCodeId: string; codeName: string }) => {
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
        
        // 사용자가 찜한 레시피 목록 가져오기
        let favoriteRecipeIds: number[] = [];
        if (currentMemberId.value) {
            try {
                const favoritesResponse = await httpJson(
                    getApiBaseUrl('cook'),
                    `/api/recipe/favorites/${currentMemberId.value}`,
                    { method: 'GET' }
                );
                
                if (Array.isArray(favoritesResponse)) {
                    favoriteRecipeIds = favoritesResponse.map((fav: any) => fav.recipeId);
                } else if (favoritesResponse.data && Array.isArray(favoritesResponse.data)) {
                    favoriteRecipeIds = favoritesResponse.data.map((fav: any) => fav.recipeId);
                }
            } catch (err) {
                console.error('찜 목록을 가져올 수 없습니다:', err);
            }
        }
        
        recipes.value = searchResults.map((recipe: any) => {
            const cookingTime = extractCookingTime(recipe.cookingTips);
            const servings = extractServings(recipe.cookingTips);

            const isFavorite = favoriteRecipeIds.includes(recipe.id);
            
            const categoryKeys = extractCategoryKeys(recipe);
            const categoryIds = extractCategoryIds(recipe);
            const keywordCategory = recipe.categories?.find((cat: any) => cat.codeId === 'COOKING_KEYWORD');
            const primaryCategoryId = keywordCategory?.detailCodeId || categoryIds[0] || null;

            return {
                ...recipe,
                categoryKeys,
                categoryIds,
                category: primaryCategoryId,
                cookingTime,
                servings,
                isFavorite,
                commentCount: recipe.commentCount || 0
            };
        });

        first.value = 0; // 검색 시 첫 페이지로 이동
    } catch (err: any) {
        console.error('검색 실패:', err);
        error.value = err.message || '검색 중 오류가 발생했습니다.';
        recipes.value = [];
    } finally {
        loading.value = false;
    }
};

// 조회수/댓글 개수 포맷팅
const formatCount = (count: number | undefined) => {
    if (!count || count === 0) return null;
    
    if (count >= 100000000) {
        const eok = count / 100000000;
        const rounded = Math.round(eok * 10) / 10;
        if (rounded % 1 === 0) {
            return `${Math.round(rounded)}억`;
        }
        return `${rounded}억`;
    }
    
    if (count >= 10000) {
        const man = count / 10000;
        const rounded = Math.round(man * 10) / 10;
        if (rounded % 1 === 0) {
            return `${Math.round(rounded)}만`;
        }
        return `${rounded}만`;
    }
    
    return count.toLocaleString();
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

        const response = await httpJson(
            getApiBaseUrl('cook'),
            `/api/recipe/favorites/toggle?memberId=${currentMemberId.value}&recipeId=${recipeId}`,
            { method: 'PUT' }
        );

        (recipe as any).isFavorite = response.isFavorite;
    } catch (err) {
        console.error('찜 토글 실패:', err);
    }
};

// 레시피 상세 조회
const viewRecipe = (recipeId: number) => {
    router.push(`/recipe/${recipeId}`);
};

// 북마크 추가
const bookmarkRecipe = (_recipeId: number) => {
    // 북마크 기능은 추후 구현 예정
};

// 페이징
const onPageChange = (event: any) => {
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
.recipe-section {
    margin-top: 2rem;
    padding-top: 2rem;
    border-top: 1px solid var(--surface-border);
}

/* 레시피 그리드 스타일 */
.recipe-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 1.5rem;
    margin-bottom: 2rem;
}

.recipe-card-wrapper {
    transition: transform 0.2s ease;
    cursor: pointer;
}

.recipe-card-wrapper:hover {
    transform: translateY(-4px);
}

.recipe-card {
    border-radius: 16px;
    overflow: hidden;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
    transition: all 0.3s ease;
}

.recipe-card:hover {
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
}

/* 레시피 이미지 스타일 */
.recipe-image-container {
    position: relative;
    width: 100%;
    height: 300px;
    overflow: hidden;
    display: flex;
    align-items: center;
    justify-content: center;
}

.recipe-image {
    width: 100%;
    height: 100%;
    min-width: 100%;
    min-height: 100%;
    object-fit: cover;
    object-position: center;
    transition: transform 0.3s ease;
    display: block;
}

.recipe-card:hover .recipe-image {
    transform: scale(1.05);
}

.recipe-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(to bottom, rgba(0, 0, 0, 0.3) 0%, transparent 50%, rgba(0, 0, 0, 0.5) 100%);
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    padding: 1rem;
    opacity: 0;
    transition: opacity 0.3s ease;
}

.recipe-card:hover .recipe-overlay {
    opacity: 1;
}

.recipe-actions {
    display: flex;
    gap: 0.5rem;
    justify-content: flex-end;
}

.recipe-category-tag {
    align-self: flex-start;
}

.recipe-hits-overlay {
    position: absolute;
    bottom: 0;
    right: 0;
    padding: 0.5rem 1rem;
    background: rgba(0, 0, 0, 0.6);
    color: white;
    font-size: 0.85rem;
    border-radius: 8px 0 0 0;
    z-index: 1;
}

.recipe-comment-count {
    text-align: left;
}

/* 레시피 콘텐츠 스타일 */
.recipe-content {
    padding: 1rem;
}

.recipe-title {
    font-size: 1.25rem;
    font-weight: 600;
    color: var(--text-color);
    margin: 0 0 1rem 0;
    line-height: 1.4;
}

.recipe-title :deep(mark) {
    background-color: #fef08a;
    padding: 0 2px;
    border-radius: 2px;
    font-weight: inherit;
    font-size: inherit;
    font-family: inherit;
    color: inherit;
    line-height: inherit;
}

/* 리스트 뷰의 mark 태그 스타일 */
.recipe-list-item h4 :deep(mark) {
    background-color: #fef08a;
    padding: 0 2px;
    border-radius: 2px;
    font-weight: inherit;
    font-size: inherit;
    font-family: inherit;
    color: inherit;
    line-height: inherit;
}

.recipe-meta {
    display: flex;
    flex-direction: column;
    gap: 0.75rem;
}

.recipe-rating {
    display: flex;
    align-items: center;
    gap: 0.5rem;
}

.rating-text {
    font-size: 0.9rem;
    font-weight: 600;
    color: var(--text-color);
}

.recipe-info {
    display: flex;
    gap: 1rem;
}

.info-item {
    display: flex;
    align-items: center;
    gap: 0.25rem;
    font-size: 0.85rem;
    color: var(--text-color-secondary);
}

.info-item i {
    font-size: 0.8rem;
}

/* 리스트 뷰 스타일 */
.recipe-list-item {
    border-bottom: 1px solid var(--surface-border);
}

.recipe-list-item:last-child {
    border-bottom: none;
}

.recipe-thumbnail {
    width: 80px;
    height: 80px;
    min-width: 80px;
    min-height: 80px;
    object-fit: cover;
    object-position: center;
    border-radius: 8px;
    display: block;
    flex-shrink: 0;
}

/* 반응형 디자인 */
@media (max-width: 768px) {
    .recipe-grid {
        grid-template-columns: 1fr;
        gap: 1rem;
    }
}
</style>

