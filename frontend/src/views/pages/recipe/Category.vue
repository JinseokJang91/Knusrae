<template>
    <div class="card">
        <!-- header : 페이지 제목, 새로고침 버튼, 상세검색 다이얼로그 버튼 -->
        <div class="flex justify-between items-center mb-4">
            <h1 class="text-3xl font-bold text-gray-900">카테고리</h1>
        </div>

        <!-- header : 카테고리 선택 영역 (좌측 메인 카테고리, 우측 서브 카테고리) -->
        <div class="category-selector mb-4">
            <div class="flex gap-4">
                <!-- 좌측: 메인 카테고리 목록 (수직 일렬) -->
                <div class="main-categories">
                    <div
                        v-for="mainCategory in mainCategories"
                        :key="mainCategory.codeId"
                        :class="['main-category-item', selectedMainCategory === mainCategory.codeId ? 'selected' : '']"
                        @click="selectMainCategory(mainCategory.codeId)"
                    >
                        {{ mainCategory.codeName }}
                    </div>
                </div>

                <!-- 우측: 선택된 메인 카테고리의 서브 카테고리 목록 (나열) -->
                <div class="sub-categories flex-1">
                    <div class="flex flex-wrap gap-2">
                        <Button
                            v-for="detail in selectedMainCategoryDetails"
                            :key="detail.detailCodeId"
                            :label="detail.codeName"
                            :class="selectedCategory === detail.detailCodeId ? 'p-button-primary' : 'p-button-outlined'"
                            size="small"
                            @click="selectCategory(detail.detailCodeId)"
                            class="category-button"
                        />
                    </div>
                </div>
            </div>
        </div>

        <!-- body : 레시피 목록 섹션 -->
        <div class="recipe-section">
            <div class="flex justify-between items-center mb-3">
                <h2 class="text-2xl font-semibold text-gray-900 m-0">
                    {{ getCategoryTitle() + '(' + totalDisplayRecipes + ')' }}
                </h2>
                <div class="flex gap-2">
                    <Button icon="pi pi-th-large" :class="viewMode === 'grid' ? 'p-button-primary' : 'p-button-secondary'" size="small" @click="viewMode = 'grid'" />
                    <Button icon="pi pi-list" :class="viewMode === 'list' ? 'p-button-primary' : 'p-button-secondary'" size="small" @click="viewMode = 'list'" />
                </div>
            </div>

            <!-- 로딩 상태 -->
            <div v-if="loading" class="text-center py-8">
                <ProgressSpinner />
                <p class="text-gray-600 mt-3">레시피를 불러오는 중...</p>
            </div>

            <!-- 에러 상태 -->
            <div v-else-if="error" class="text-center py-8">
                <i class="pi pi-exclamation-triangle text-6xl text-red-500 mb-4"></i>
                <h3 class="text-2xl font-semibold text-gray-600 mb-2">레시피를 불러올 수 없습니다</h3>
                <p class="text-gray-600 mb-4">{{ error }}</p>
                <Button label="다시 시도" @click="loadRecipes" />
            </div>

            <!-- 레시피 목록이 있는 경우 -->
            <div v-else-if="displayRecipes.length > 0">
                <!-- 그리드 뷰 (카드 형태) -->
                <div v-if="viewMode === 'grid'" class="recipe-grid">
                    <div v-for="recipe in displayRecipes" :key="recipe.id" class="recipe-card-wrapper" @click="viewRecipe(recipe.id)">
                        <Card class="recipe-card h-full">
                            <template #header>
                                <div class="recipe-image-container">
                                    <img :src="recipe.thumbnail" :alt="recipe.title" class="recipe-image" />
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
                                    <h3 class="recipe-title">{{ recipe.title }}</h3>
                                    <div class="recipe-meta">
                                        <div v-if="recipe.hasReviews && recipe.rating" class="recipe-rating">
                                            <Rating :modelValue="recipe.rating" readonly :cancel="false" />
                                            <span class="rating-text">{{ recipe.rating.toFixed(1) }}</span>
                                        </div>
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
                                        <!-- 댓글 개수 표시 (닉네임 하단, 좌측 정렬) -->
                                        <div v-if="formatCount(recipe.commentCount)" class="recipe-comment-count mt-1">
                                            <span class="text-sm text-gray-600 cursor-pointer hover:text-primary" @click.stop="scrollToComments(recipe.id)">
                                                댓글 {{ formatCount(recipe.commentCount) }}
                                            </span>
                                        </div>
                                    </div>
                                </div>
                            </template>
                            <template #footer>
                                <!-- <Button label="상세보기" class="w-full" @click="viewRecipe(recipe.id)" /> -->
                            </template>
                        </Card>
                    </div>
                </div>

                <!-- 리스트 뷰 -->
                <div v-else class="recipe-list">
                    <div v-for="recipe in displayRecipes" :key="recipe.id" class="recipe-list-item">
                        <div class="flex items-center gap-3 p-3 rounded hover:bg-gray-50 transition-colors duration-150">
                            <img :src="recipe.thumbnail" :alt="recipe.title" class="recipe-thumbnail" />
                            <div class="flex-1">
                                <h4 class="text-lg font-semibold text-gray-900 m-0 mb-1">{{ recipe.title }}</h4>
                                <div class="flex items-center gap-3 text-sm text-gray-500">
                                    <div v-if="recipe.cookingTime" class="flex items-center gap-1">
                                        <i class="pi pi-clock"></i>
                                        <span>{{ recipe.cookingTime }}</span>
                                    </div>
                                    <div v-if="recipe.servings" class="flex items-center gap-1">
                                        <i class="pi pi-users"></i>
                                        <span>{{ recipe.servings }}</span>
                                    </div>
                                    <div v-if="recipe.hasReviews && recipe.rating" class="flex items-center gap-1">
                                        <i class="pi pi-star-fill text-yellow-500"></i>
                                        <span>{{ recipe.rating.toFixed(1) }}</span>
                                    </div>
                                    <Tag v-if="getCategoryName(recipe.category)" :value="getCategoryName(recipe.category)" severity="info" />
                                    <!-- 조회수 표시 (키워드 태그 우측) -->
                                    <span v-if="formatCount(recipe.hits)" class="text-sm text-gray-600">
                                        조회수 {{ formatCount(recipe.hits) }}
                                    </span>
                                    <!-- 댓글 개수 표시 (조회수 우측) -->
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
                <i class="pi pi-book text-6xl text-300 mb-4"></i>
                <h3 class="text-2xl font-semibold text-gray-600 mb-2">레시피가 없습니다</h3>
                <p class="text-gray-600 mb-4">{{ selectedCategory ? '선택한 카테고리에 레시피가 없습니다.' : '등록된 레시피가 없습니다.' }}</p>
            </div>
        </div>
    </div>
</template>

<script setup>
import { httpJson } from '@/utils/http';
import { useAuthStore } from '@/stores/authStore';
import Button from 'primevue/button';
import Card from 'primevue/card';
import Dialog from 'primevue/dialog';
import Dropdown from 'primevue/dropdown';
import InputText from 'primevue/inputtext';
import Paginator from 'primevue/paginator';
import ProgressSpinner from 'primevue/progressspinner';
import Rating from 'primevue/rating';
import Tag from 'primevue/tag';
import { useToast } from 'primevue/usetoast';
import { computed, onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const toast = useToast();
const authStore = useAuthStore();

// 반응형 데이터
const categories = ref([]);
const mainCategories = ref([]); // 메인 카테고리 목록 (codeName 필드를 가진 항목들)
const recipes = ref([]);
const selectedCategory = ref(null);
const selectedMainCategory = ref(null); // 선택된 메인 카테고리
const searchResults = ref([]);
const searchQuery = ref('');
const searchCategory = ref(null);
const searchDifficulty = ref(null);
const viewMode = ref('grid');
const first = ref(0);
const rows = ref(12);
const loading = ref(false);
const error = ref(null);

// 현재 로그인한 사용자 정보 (authStore에서 가져옴)
const isLoggedIn = computed(() => authStore.isLoggedIn);
const currentMemberInfo = computed(() => authStore.memberInfo);
const currentMemberId = computed(() => authStore.memberInfo?.id || null);

// API 기본 URL
// TODO 테스트는 로컬로 진행, 추후 분기처리로 EC2 연결 예정
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8082';

// Enum Class - Difficulty (난이도)
const difficulties = ref([
    { name: '매우 쉬움', value: 'very easy' },
    { name: '쉬움', value: 'easy' },
    { name: '보통', value: 'medium' },
    { name: '조금 어려움', value: 'little hard' },
    { name: '어려움', value: 'hard' }
]);

// 카테고리 header > 표시된 레시피 > 개수
const filteredRecipes = computed(() => {
    let filtered = recipes.value;
    
    // 서브 카테고리가 선택된 경우 해당 서브 카테고리로 필터링
    if (selectedCategory.value) {
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

// 카테고리 body > 레시피
const displayRecipes = computed(() => {
    const source = searchResults.value.length > 0 ? searchResults.value : filteredRecipes.value;
    return source.slice(first.value, first.value + rows.value);
});

// 카테고리 footer > 페이징 처리
const totalDisplayRecipes = computed(() => {
    return searchResults.value.length > 0 ? searchResults.value.length : filteredRecipes.value.length;
});

// 선택된 메인 카테고리의 서브 카테고리 목록 (details)
const selectedMainCategoryDetails = computed(() => {
    if (!selectedMainCategory.value) {
        return [];
    }
    const mainCategory = mainCategories.value.find((cat) => cat.codeId === selectedMainCategory.value);
    return mainCategory?.details || [];
});

// Function > onMounted > 카테고리 조회
// TODO 카테고리 목록 조회 API 연결 예정
const loadCategories = async () => {
    try {
        const response = await httpJson(API_BASE_URL, '/api/common-codes?codeGroup=CATEGORY', {
            method: 'GET'
        });

        const codes = Array.isArray(response) ? response : [];
        
        // 메인 카테고리 목록 저장 (codeName 필드를 가진 모든 항목들)
        mainCategories.value = codes.map((code) => ({
            codeId: code.codeId,
            codeName: code.codeName,
            details: code.details || []
        }));

        // 모든 메인 카테고리의 서브 카테고리를 categories 배열에 저장
        categories.value = [];
        codes.forEach((code) => {
            if (Array.isArray(code.details)) {
                code.details.forEach((detail) => {
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

        // 첫 번째 메인 카테고리를 기본 선택
        if (mainCategories.value.length > 0) {
            selectedMainCategory.value = mainCategories.value[0].codeId;
        }
    } catch (error) {
        console.warn('카테고리 목록 조회 실패, 기본값을 사용합니다.', error);
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

// Function > onMounted > 레시피 조회
// 레시피의 모든 카테고리 키(codeId-detailCodeId)들을 추출하는 함수
const extractCategoryKeys = (recipe) => {
    if (!recipe || !recipe.categories || recipe.categories.length === 0) {
        return [];
    }
    // 모든 카테고리의 "codeId-detailCodeId" 형식 문자열을 배열로 반환
    return recipe.categories
        .filter((category) => category.codeId && category.detailCodeId)
        .map((category) => `${category.codeId}-${category.detailCodeId}`);
};

// 레시피의 모든 카테고리 detailCodeId들을 추출하는 함수 (표시용)
const extractCategoryIds = (recipe) => {
    if (!recipe || !recipe.categories || recipe.categories.length === 0) {
        return [];
    }
    // 모든 카테고리의 detailCodeId를 배열로 반환
    return recipe.categories.map((category) => category.detailCodeId || category.codeId).filter(Boolean);
};

// Function > cookingTips에서 요리 시간 추출
const extractCookingTime = (cookingTips) => {
    if (!cookingTips || !Array.isArray(cookingTips)) {
        return null;
    }
    const cookingTimeTip = cookingTips.find((tip) => tip.codeId === 'COOKING_TIME');
    return cookingTimeTip?.detailName || null;
};

// Function > cookingTips에서 인분 수 추출
const extractServings = (cookingTips) => {
    if (!cookingTips || !Array.isArray(cookingTips)) {
        return null;
    }
    const servingTip = cookingTips.find((tip) => tip.codeId === 'SERVINGS');
    return servingTip?.detailName || null;
};

const loadRecipes = async () => {
    try {
        loading.value = true;
        error.value = null;

        const response = await httpJson(API_BASE_URL, '/api/recipe/list/all', {
            method: 'GET'
        });

        const data = response.data || response || [];
        
        // 사용자가 찜한 레시피 목록 가져오기
        let favoriteRecipeIds = [];
        if (currentMemberId.value) {
            try {
                // 백엔드 API: GET /api/recipe/favorites/{memberId}
                const favoritesResponse = await httpJson(
                    import.meta.env.VITE_API_BASE_URL_COOK,
                    `/api/recipe/favorites/${currentMemberId.value}`,
                    { method: 'GET' }
                );
                
                // API 응답이 배열인지 확인하고 recipeId 추출
                if (Array.isArray(favoritesResponse)) {
                    favoriteRecipeIds = favoritesResponse.map((fav) => fav.recipeId);
                } else if (favoritesResponse.data && Array.isArray(favoritesResponse.data)) {
                    favoriteRecipeIds = favoritesResponse.data.map((fav) => fav.recipeId);
                }
            } catch (err) {
                console.error('찜 목록을 가져올 수 없습니다:', err);
            }
        }
        
        recipes.value = data.map((recipe) => {
            // cookingTips에서 SERVING과 COOKING_TIME 추출
            const cookingTime = extractCookingTime(recipe.cookingTips);
            const servings = extractServings(recipe.cookingTips);
            
            // 후기가 있을 때만 평균 별점 계산 (일단 reviews 필드가 없으므로 나중에 API에서 받을 것으로 가정)
            const averageRating = recipe.reviews && recipe.reviews.length > 0
                ? recipe.reviews.reduce((sum, review) => sum + (review.rating || 0), 0) / recipe.reviews.length
                : null;

            const isFavorite = favoriteRecipeIds.includes(recipe.id);
            
            // 레시피의 모든 카테고리 키(codeId-detailCodeId) 추출
            const categoryKeys = extractCategoryKeys(recipe);
            // 레시피의 모든 카테고리 ID 추출 (표시용)
            const categoryIds = extractCategoryIds(recipe);
            // 표시용 대표 카테고리 (첫 번째 카테고리 또는 COOKING_KEYWORD 우선)
            const keywordCategory = recipe.categories?.find((cat) => cat.codeId === 'COOKING_KEYWORD');
            const primaryCategoryId = keywordCategory?.detailCodeId || categoryIds[0] || null;

            return {
                ...recipe,
                categoryKeys, // 모든 카테고리 키 배열 (필터링용, "codeId-detailCodeId" 형식)
                categoryIds, // 모든 카테고리 ID 배열 (표시용)
                category: primaryCategoryId, // 표시용 대표 카테고리
                cookingTime,
                servings,
                rating: averageRating,
                hasReviews: recipe.reviews && recipe.reviews.length > 0,
                isFavorite,
                commentCount: recipe.commentCount || 0
            };
        });

        toast.add({
            severity: 'success',
            summary: '레시피 로드 완료',
            detail: `${recipes.value.length}개의 레시피를 불러왔습니다.`,
            life: 3000
        });
    } catch (err) {
        console.error('레시피 로드 실패:', err);
        error.value = err.message || '레시피를 불러오는데 실패했습니다.';

        // API 실패 시 더미 데이터 사용
        recipes.value = [
            {
                id: 1,
                title: '김치찌개',
                thumbnail: 'https://images.unsplash.com/photo-1565299624946-b28f40a0ca4b?w=400',
                category: 'korean',
                categoryIds: ['korean'],
                categoryKeys: ['COOKING_KEYWORD-korean'],
                cookingTime: '30분',
                servings: '2인분',
                hits: 1250,
                rating: 4.5,
                hasReviews: true,
                isFavorite: false,
                commentCount: 173
            },
            {
                id: 2,
                title: '짜장면',
                thumbnail: 'https://images.unsplash.com/photo-1563379091339-03246963d4d8?w=400',
                category: 'chinese',
                categoryIds: ['chinese'],
                categoryKeys: ['COOKING_KEYWORD-chinese'],
                cookingTime: '20분',
                servings: '2인분',
                hits: 980,
                rating: 4.3,
                hasReviews: true,
                isFavorite: false,
                commentCount: 45
            },
            {
                id: 3,
                title: '초밥',
                thumbnail: 'https://images.unsplash.com/photo-1579584425555-c3ce17fd4351?w=400',
                category: 'japanese',
                categoryIds: ['japanese'],
                categoryKeys: ['COOKING_KEYWORD-japanese'],
                cookingTime: '45분',
                servings: '4인분',
                hits: 2100,
                rating: 4.8,
                hasReviews: true,
                isFavorite: false,
                commentCount: 1435
            },
            {
                id: 4,
                title: '파스타',
                thumbnail: 'https://images.unsplash.com/photo-1621996346565-e3dbc353d2e5?w=400',
                category: 'western',
                categoryIds: ['western'],
                categoryKeys: ['COOKING_KEYWORD-western'],
                cookingTime: '25분',
                servings: '2인분',
                hits: 1560,
                rating: 4.2,
                hasReviews: true,
                isFavorite: true,
                commentCount: 0
            },
            {
                id: 5,
                title: '치즈케이크',
                thumbnail: 'https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=400',
                category: 'dessert',
                categoryIds: ['dessert'],
                categoryKeys: ['COOKING_KEYWORD-dessert'],
                cookingTime: '90분',
                servings: '8인분',
                hits: 890,
                rating: null,
                hasReviews: false,
                isFavorite: true,
                commentCount: 12
            },
            {
                id: 6,
                title: '불고기',
                thumbnail: 'https://images.unsplash.com/photo-1529042410759-befb1204b468?w=400',
                category: 'korean',
                categoryIds: ['korean'],
                categoryKeys: ['COOKING_KEYWORD-korean'],
                cookingTime: '15분',
                servings: '3인분',
                hits: 1750,
                rating: null,
                hasReviews: false,
                isFavorite: false,
                commentCount: 89
            }
        ];

        toast.add({
            severity: 'warn',
            summary: 'API 연결 실패',
            detail: '더미 데이터를 표시합니다.',
            life: 3000
        });
    } finally {
        loading.value = false;
    }
};

// Function > Button > 메인 카테고리 선택
const selectMainCategory = (codeId) => {
    selectedMainCategory.value = codeId;
    selectedCategory.value = null; // 메인 카테고리 변경 시 서브 카테고리 선택 해제
    searchResults.value = [];
    first.value = 0;
    
    // 선택된 메인 카테고리의 서브 카테고리 목록 출력
    const mainCategory = mainCategories.value.find((cat) => cat.codeId === codeId);
};

// Function > Button > body 카테고리 선택 시 목록 필터링
const selectCategory = (categoryValue) => {
    selectedCategory.value = selectedCategory.value === categoryValue ? null : categoryValue;
    
    // 필터링된 레시피 개수 출력
    const mainCategoryDetailIds = selectedMainCategoryDetails.value.map(detail => detail.detailCodeId);
    
    searchResults.value = [];
    first.value = 0;
};

// const viewCategory = (categoryValue) => {
//     selectedCategory.value = categoryValue;
//     searchResults.value = [];
//     first.value = 0;
// };

// Function > header 현재 카테고리 값 동기화 (COOKING_KEYWORD인 경우만 반환)
const getCategoryName = (categoryValue) => {
    // COOKING_KEYWORD 메인 카테고리에 속한 카테고리만 반환
    const category = categories.value.find((cat) => cat.value === categoryValue && cat.mainCategoryId === 'COOKING_KEYWORD');
    return category ? category.name : null;
};

// Function > header 타이틀 생성 (메인 카테고리 + 서브 카테고리)
const getCategoryTitle = () => {
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

// Function > 조회수/댓글 개수 포맷팅 (만/억 단위 처리)
const formatCount = (count) => {
    if (!count || count === 0) return null;
    
    // 1억 이상인 경우
    if (count >= 100000000) {
        const eok = count / 100000000;
        // 백만 자리에서 반올림 (소수점 첫째 자리까지)
        const rounded = Math.round(eok * 10) / 10;
        // 소수점이 0이면 정수로 표시
        if (rounded % 1 === 0) {
            return `${Math.round(rounded)}억`;
        }
        return `${rounded}억`;
    }
    
    // 1만 이상인 경우
    if (count >= 10000) {
        const man = count / 10000;
        // 백의 자리에서 반올림 (소수점 첫째 자리까지)
        const rounded = Math.round(man * 10) / 10;
        // 소수점이 0이면 정수로 표시
        if (rounded % 1 === 0) {
            return `${Math.round(rounded)}만`;
        }
        return `${rounded}만`;
    }
    
    // 1만 미만인 경우
    return count.toLocaleString();
};

// Function > 레시피 상세 페이지 댓글 영역으로 이동
const scrollToComments = (recipeId) => {
    router.push(`/recipe/${recipeId}#comments`);
};

// Function > Button > 찜 목록 추가
const toggleFavorite = async (recipeId) => {
    // 로그인 확인
    if (!currentMemberId.value) {
        toast.add({
            severity: 'warn',
            summary: '로그인 필요',
            detail: '찜 기능을 사용하려면 로그인이 필요합니다.',
            life: 3000
        });
        return;
    }

    try {
        const recipe = recipes.value.find((r) => r.id === recipeId);
        if (!recipe) return;

        // API 호출
        const response = await httpJson(
            import.meta.env.VITE_API_BASE_URL_COOK,
            `/api/recipe/favorites/toggle?memberId=${currentMemberId.value}&recipeId=${recipeId}`,
            { method: 'PUT' }
        );

        // 상태 업데이트
        recipe.isFavorite = response.isFavorite;

        toast.add({
            severity: 'success',
            summary: recipe.isFavorite ? '찜 추가' : '찜 해제',
            detail: recipe.isFavorite ? '찜 목록에 추가되었습니다.' : '찜 목록에서 제거되었습니다.',
            life: 3000
        });
    } catch (err) {
        console.error('찜 토글 실패:', err);
        toast.add({
            severity: 'error',
            summary: '오류 발생',
            detail: '찜 기능을 사용할 수 없습니다.',
            life: 3000
        });
    }
};

// Function > 레시피 상세 조회
const viewRecipe = (recipeId) => {
    router.push(`/recipe/${recipeId}`);
};

// Function > Button > 북마크 추가
// TODO 북마크 목록 데이터로 관리 시 API 연결 필요
const bookmarkRecipe = (recipeId) => {
    toast.add({ severity: 'info', summary: '북마크', detail: '레시피가 북마크되었습니다.', life: 3000 });
};

// Function > 페이징
const onPageChange = (event) => {
    first.value = event.first;
    rows.value = event.rows;
};

// 카테고리 선택 감시
watch(selectedCategory, (newCategory, oldCategory) => {
    first.value = 0; // 페이지 초기화
});

// 생명주기
onMounted(() => {
    // 로그인 여부와 관계없이 카테고리 및 레시피 조회
    const initializeData = async () => {
        await loadCategories();
        await loadRecipes();
    };
    initializeData();
});
</script>

<style scoped>
.stat-card {
    background: linear-gradient(135deg, #249461 0%, #afe6ca 100%);
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

.recipe-card {
    transition:
        transform 0.2s,
        box-shadow 0.2s;
}

.recipe-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

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

.recipe-section {
    margin-top: 2rem;
    padding-top: 2rem;
    border-top: 1px solid var(--surface-border);
}

/* 카테고리 선택기 스타일 */
.category-selector {
    background: var(--surface-card);
    border: 1px solid var(--surface-border);
    border-radius: 12px;
    padding: 1.5rem;
    margin-bottom: 2rem;
}

/* 좌측 메인 카테고리 스타일 */
.main-categories {
    min-width: 200px;
    border-right: 2px solid var(--surface-border);
    padding-right: 1rem;
    margin-right: 1rem;
}

.main-category-item {
    padding: 0.75rem 1rem;
    margin-bottom: 0.5rem;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.2s ease;
    font-weight: 500;
    color: var(--text-color);
    background: var(--surface-ground);
    border: 1px solid transparent;
}

.main-category-item:hover {
    background: var(--surface-hover);
    border-color: var(--primary-color);
}

.main-category-item.selected {
    background: var(--primary-color);
    color: white;
    border-color: var(--primary-color);
    font-weight: 600;
}

/* 우측 서브 카테고리 스타일 */
.sub-categories {
    padding-left: 1rem;
}

.category-button {
    min-width: 120px;
    transition: all 0.2s ease;
}

.category-button:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
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

.recipe-info-tag {
    font-size: 0.85rem;
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

/* 반응형 디자인 */
@media (max-width: 768px) {
    .recipe-grid {
        grid-template-columns: 1fr;
        gap: 1rem;
    }

    .category-selector {
        padding: 1rem;
    }

    .category-selector .flex {
        flex-direction: column;
    }

    .main-categories {
        min-width: 100%;
        border-right: none;
        border-bottom: 2px solid var(--surface-border);
        padding-right: 0;
        padding-bottom: 1rem;
        margin-right: 0;
        margin-bottom: 1rem;
    }

    .sub-categories {
        padding-left: 0;
    }

    .category-button {
        min-width: 100px;
        font-size: 0.9rem;
    }
}
</style>
